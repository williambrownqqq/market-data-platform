package com.market.data.platform.exception;

import java.util.function.Supplier;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public static Supplier<EntityNotFoundException> supplier(String message) {
        return () -> new EntityNotFoundException(message);
    }
}
