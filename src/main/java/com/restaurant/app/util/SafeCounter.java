package com.restaurant.app.util;

import java.util.concurrent.atomic.AtomicInteger;


public class SafeCounter {

    private final AtomicInteger value = new AtomicInteger();

    public void increment() {
        value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }
}
