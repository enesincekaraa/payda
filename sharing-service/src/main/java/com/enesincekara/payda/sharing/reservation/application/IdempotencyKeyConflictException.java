package com.enesincekara.payda.sharing.reservation.application;

public class IdempotencyKeyConflictException extends RuntimeException {

    public IdempotencyKeyConflictException() {
        super("Idempotency key was already used for a different reservation request");
    }
}