package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.RaceConditionDemoResult;

public interface ConcurrencyDemoService {

    RaceConditionDemoResult runUnsafeCounterDemo(int threads, int incrementsPerThread);

    RaceConditionDemoResult runAtomicCounterDemo(int threads, int incrementsPerThread);
}
