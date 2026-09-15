package com.hrutvik.payments.application;

public class RateLimitExceededException extends RuntimeException {
  private final long retryAfterSeconds;
  public RateLimitExceededException(long retryAfterSeconds){
    super("Merchant payment creation rate exceeded");
    this.retryAfterSeconds=retryAfterSeconds;
  }
  public long getRetryAfterSeconds(){return retryAfterSeconds;}
}
