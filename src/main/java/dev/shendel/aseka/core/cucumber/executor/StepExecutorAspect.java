package dev.shendel.aseka.core.cucumber.executor;

import dev.shendel.aseka.core.configuration.AsekaProperties;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.exception.ExceptionWrapper;
import dev.shendel.aseka.core.service.RetryExecutor;
import io.cucumber.java.StepDefinitionAnnotation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static dev.shendel.aseka.core.service.RetryExecutor.DEFAULT_RETRY_SECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StepExecutorAspect {

    private final AsekaProperties properties;
    private final StepChainExecutor stepChainExecutor;

    @SneakyThrows
    @Around("execution(@(@io.cucumber.java.StepDefinitionAnnotation *) * *(..)) " +
            "&& !@annotation(dev.shendel.aseka.core.cucumber.executor.StepExecutorIgnore)")
    public Object addStepsToExecutor(ProceedingJoinPoint joinPoint) {
        validateStep(joinPoint);
        if (stepChainExecutor.isActive()) {
            stepChainExecutor.addStep(wrapToRunnable(joinPoint));
        } else if (isRetryable(joinPoint)) {
            int retrySeconds = getRetrySeconds(joinPoint);
            RetryExecutor.of(retrySeconds)
                         .retryExceptions(AssertionError.class)
                         .execute(Unchecked.runnable(joinPoint::proceed));
        } else {
            joinPoint.proceed();
        }
        return null;
    }

    private boolean isRetryable(ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        return method.getAnnotation(RetryableStep.class) != null;
    }

    //TODO добавить DEFAULT_RETRY_SECONDS в application properties, а так же возможность устанавливать в контексте
    private int getRetrySeconds(ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        RetryableStep retryableStepAnnotation = method.getAnnotation(RetryableStep.class);
        String annotationRetrySeconds = retryableStepAnnotation.defaultRetrySeconds();
        if (isBlank(annotationRetrySeconds)) {
            return DEFAULT_RETRY_SECONDS;
        } else {
            return Integer.parseInt(annotationRetrySeconds);
        }
    }

    private void validateStep(ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        Deprecated deprecated = method.getAnnotation(Deprecated.class);

        if (properties.isDeprecatedFeaturesEnabled() && deprecated != null) {
            log.warn("Step will be deleted in next version");
        } else if (deprecated != null) {
            throw new AsekaException("Step will be deleted in next version");
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    private RunnableStep wrapToRunnable(ProceedingJoinPoint joinPoint) {
        return new RunnableStep() {
            @Override
            public String getStepExpression() {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                return StepExecutorAspect.getStepExpression(signature);
            }

            @Override
            public void run() {
                try {
                    joinPoint.proceed();
                } catch (Throwable throwable) {
                    ExceptionWrapper.sneakyThrow(throwable);
                }
            }
        };
    }

    private static String getStepExpression(MethodSignature signature) {
        try {
            Method method = signature.getMethod();
            for (Annotation annotation : method.getAnnotations()) {
                if (isStepDefinitionAnnotation(annotation)) {
                    Method expressionMethod = annotation.getClass().getMethod("value");
                    return (String) expressionMethod.invoke(annotation);
                }
            }
            log.error("Can't parse step expression");
            return "error";
        } catch (Exception exception) {
            log.error("Can't parse step expression", exception);
            return "error";
        }
    }

    private static boolean isStepDefinitionAnnotation(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(StepDefinitionAnnotation.class);
    }

}