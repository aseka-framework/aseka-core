package dev.shendel.aseka.core.service;

import dev.shendel.aseka.core.exception.ExceptionWrapper;
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

    private final RetryConfig retryConfig;
    private boolean retryAnyException = false;

    public static RetryExecutor of(int retrySeconds) {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(retrySeconds * 1000 / DEFAULT_POLL_INTERVAL)
                .waitDuration(Duration.ofMillis(DEFAULT_POLL_INTERVAL))
                .build();
        return new RetryExecutor(retryConfig);
    }

    public RetryExecutor retryAnyException() {
        retryAnyException = true;
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
                context.onComplete();
                break;
            } catch (Throwable throwable) {
                if (throwable instanceof AssertionError || retryAnyException) {
                    try {
                        context.onError(ExceptionWrapper.wrap(throwable));
                    } catch (ExceptionWrapper wrapper) {
                        throw wrapper.unwrap();
                    }
                } else {
                    throw throwable;
                }
            }
        } while (true);
    }

}
