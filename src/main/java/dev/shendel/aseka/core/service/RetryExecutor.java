package dev.shendel.aseka.core.service;

import dev.shendel.aseka.core.exception.ExceptionWrapper;
import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class RetryExecutor {

    public final static int DEFAULT_RETRY_SECONDS = 3;
    public final static int DEFAULT_POLL_INTERVAL = 500;

    private RetryConfig retryConfig;
    private Class<? extends Throwable>[] retryExceptions = new Class[0];
    private boolean retryAnyException = false;
    private boolean retryUntilTheEnd = false;

    public static RetryExecutor of(int maxRetrySeconds) {
        RetryConfig retryConfig = RetryConfig
                .custom()
                .maxAttempts(maxRetrySeconds * 1000 / DEFAULT_POLL_INTERVAL)
                .waitDuration(Duration.ofMillis(DEFAULT_POLL_INTERVAL))
                .build();
        RetryExecutor executor = new RetryExecutor();
        executor.retryConfig = retryConfig;
        return executor;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final RetryExecutor retryExceptions(@Nullable Class<? extends Throwable>... errorClasses) {
        this.retryExceptions = errorClasses != null ? errorClasses : new Class[0];
        return this;
    }

    public RetryExecutor retryAnyException() {
        retryAnyException = true;
        return this;
    }

    public RetryExecutor retryUntilTheEnd() {
        retryUntilTheEnd = true;
        retryConfig = RetryConfig.from(retryConfig)
                                 .retryOnResult(retry -> true)
                                 .build();
        return this;
    }


    @SneakyThrows
    public void execute(Runnable runnable) {
        Retry retry = Retry.of("retry", retryConfig);
        Retry.Context<Object> context = retry.context();
        retry.getEventPublisher().onSuccess(event -> {
            log.info("Attempt {} passed", event.getNumberOfRetryAttempts() + 1);
        });
        retry.getEventPublisher().onRetry(event -> {
            log.info("Attempt {} failed", event.getNumberOfRetryAttempts());
        });
        do {
            try {
                runnable.run();
                if (!retryUntilTheEnd) {
                    context.onComplete();
                    break;
                } else {
                    boolean shouldRetry = context.onResult("ignored");
                    if (!shouldRetry) {
                        context.onComplete();
                        break;
                    }
                }
            } catch (Throwable throwable) {
                if (retryAnyException || shouldRetryException(throwable)) {
                    onError(context, throwable);
                } else {
                    throw throwable;
                }
            }
        }
        while (true);
    }

    private boolean shouldRetryException(Throwable throwable) {
        for (Class<? extends Throwable> retryException : retryExceptions) {
            if (retryException.isInstance(throwable)) {
                return true;
            }
        }
        return false;
    }

    private static void onError(Retry.Context<Object> context, Throwable throwable) throws Throwable {
        try {
            context.onError(ExceptionWrapper.wrap(throwable));
        } catch (ExceptionWrapper wrapper) {
            throw wrapper.unwrap();
        }
    }

}
