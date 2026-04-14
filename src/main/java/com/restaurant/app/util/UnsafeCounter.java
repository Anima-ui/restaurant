package com.restaurant.app.util;

public class UnsafeCounter {

    private int value;

    public void increment() {
        value = value + 1;
    }

    public int getValue() {
        return value;
    }
}
