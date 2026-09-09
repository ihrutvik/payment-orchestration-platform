package com.hrutvik.payments.application;

public class IdempotencyConflictException extends RuntimeException {
  public IdempotencyConflictException(){super("Idempotency-Key was already used with a different payment request");}
}
