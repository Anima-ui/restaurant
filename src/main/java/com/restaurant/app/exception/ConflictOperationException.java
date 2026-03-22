package com.restaurant.app.exception;

public class ConflictOperationException extends RuntimeException {

    public ConflictOperationException(String message) {
        super(message);
    }
}
