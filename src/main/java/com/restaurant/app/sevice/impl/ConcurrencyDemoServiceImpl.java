package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RaceConditionDemoResult;
import com.restaurant.app.sevice.ConcurrencyDemoService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import org.springframework.stereotype.Service;

@Service
public class ConcurrencyDemoServiceImpl implements ConcurrencyDemoService {

    private static final long AWAIT_TIMEOUT_SECONDS = 30L;

    public RaceConditionDemoResult runUnsafeCounterDemo(int threads, int incrementsPerThread) {
        UnsafeCounter counter = new UnsafeCounter();
        return runDemo(
                "UNSAFE_COUNTER",
                threads,
                incrementsPerThread,
                counter::increment,
                counter::getValue,
                "Unsafe counter demonstrates race condition"
        );
    }

    public RaceConditionDemoResult runAtomicCounterDemo(int threads, int incrementsPerThread) {
        AtomicInteger counter = new AtomicInteger();
        return runDemo(
                "ATOMIC_COUNTER",
                threads,
                incrementsPerThread,
                counter::incrementAndGet,
                counter::get,
                "Atomic counter removes lost updates"
        );
    }

    private RaceConditionDemoResult runDemo(String scenario,
                                            int threads,
                                            int incrementsPerThread,
                                            Runnable incrementAction,
                                            IntSupplier valueSupplier,
                                            String note) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        try {
            for (int i = 0; i < threads; i++) {
                executorService.submit(() -> {
                    awaitLatch(startLatch);
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            incrementAction.run();
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            awaitLatch(doneLatch);

            int expectedValue = threads * incrementsPerThread;
            int actualValue = valueSupplier.getAsInt();

            return RaceConditionDemoResult.builder()
                    .scenario(scenario)
                    .threads(threads)
                    .incrementsPerThread(incrementsPerThread)
                    .expectedValue(expectedValue)
                    .actualValue(actualValue)
                    .lostUpdates(expectedValue - actualValue)
                    .note(note)
                    .build();
        } finally {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread execution was interrupted", exception);
            }
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread execution was interrupted", exception);
        }
    }

    private static final class UnsafeCounter {

        private int value;

        private void increment() {
            value = value + 1;
        }

        private int getValue() {
            return value;
        }
    }
}
