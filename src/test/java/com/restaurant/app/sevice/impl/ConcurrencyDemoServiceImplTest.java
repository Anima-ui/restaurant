package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RaceConditionDemoResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcurrencyDemoServiceImplTest {

    private final ConcurrencyDemoServiceImpl concurrencyDemoService = new ConcurrencyDemoServiceImpl();

    @Test
    void runUnsafeCounterDemoShowsRaceCondition() {
        boolean raceConditionDetected = false;
        RaceConditionDemoResult result = null;

        for (int i = 0; i < 10; i++) {
            result = concurrencyDemoService.runUnsafeCounterDemo(64, 50_000);
            if (result.getLostUpdates() > 0) {
                raceConditionDetected = true;
                break;
            }
        }

        assertThat(result).isNotNull();
        assertThat(result.getScenario()).isEqualTo("UNSAFE_COUNTER");
        assertThat(result.getActualValue()).isLessThanOrEqualTo(result.getExpectedValue());
        assertThat(raceConditionDetected).isTrue();
    }

    @Test
    void runAtomicCounterDemoKeepsExpectedValue() {
        RaceConditionDemoResult result = concurrencyDemoService.runAtomicCounterDemo(64, 1000);

        assertThat(result)
                .extracting("scenario", "expectedValue", "actualValue", "lostUpdates", "note")
                .containsExactly(
                        "ATOMIC_COUNTER",
                        64000,
                        64000,
                        0,
                        "Atomic counter removes lost updates"
                );
    }

    @Test
    void runAtomicCounterDemoThrowsWhenThreadIsInterrupted() {
        Thread.currentThread().interrupt();

        try {
            assertThatThrownBy(() -> concurrencyDemoService.runAtomicCounterDemo(64, 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("interrupted");
        } finally {
            Thread.interrupted();
        }
    }
}
