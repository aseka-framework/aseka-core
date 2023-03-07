package dev.shendel.aseka.core.service;


import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RetryExecutorTest {

    @Test
    void testExecute_retryUntilTheEnd() {
        AtomicInteger executedCount = new AtomicInteger(0);

        RetryExecutor.of(2)
                     .retryUntilTheEnd()
                     .execute(executedCount::incrementAndGet);

        assertEquals(4, executedCount.get());
    }

    @Test
    void testExecute_retrySpecificException_retryUntilLastExecutePassed() {
        AtomicInteger executedCount = new AtomicInteger(0);

        RetryExecutor.of(60)
                     .retryExceptions(AssertionError.class)
                     .execute(() -> {
                         int count = executedCount.incrementAndGet();
                         if (count != 4) {
                             throw new AssertionError();
                         }
                     });

        assertEquals(4, executedCount.get());
    }

    @Test
    void testExecute_retryAnyException_retryUntilLastExecutePassed() {
        AtomicInteger executedCount = new AtomicInteger(0);

        RetryExecutor.of(60)
                     .retryAnyException()
                     .execute(() -> {
                         int count = executedCount.incrementAndGet();
                         if (count != 4) {
                             throw new IllegalArgumentException();
                         }
                     });

        assertEquals(4, executedCount.get());
    }

    @Test
    void testExecute_NotRetryUnregisteredException() {
        AtomicInteger executedCount = new AtomicInteger(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    RetryExecutor.of(60)
                                 .execute(() -> {
                                     int count = executedCount.incrementAndGet();
                                     if (count != 4) {
                                         throw new IllegalArgumentException();
                                     }
                                 });

                }
        );

        assertEquals(1, executedCount.get());
    }

    @Test
    void testExecute_retryButReachedMaxCount() {
        AtomicInteger executedCount = new AtomicInteger(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    RetryExecutor.of(2)
                                 .retryAnyException()
                                 .execute(() -> {
                                     executedCount.incrementAndGet();
                                     throw new IllegalArgumentException();
                                 });

                }
        );

        assertEquals(4, executedCount.get());
    }

}