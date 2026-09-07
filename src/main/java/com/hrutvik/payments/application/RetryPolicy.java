package com.hrutvik.payments.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.UUID;

@Component
public class RetryPolicy {
  private final int maxAttempts; private final Duration baseDelay; private final Duration maxDelay;
  public RetryPolicy(@Value("${payments.retry.max-attempts:5}") int maxAttempts,
      @Value("${payments.retry.base-delay:30s}") Duration baseDelay,
      @Value("${payments.retry.max-delay:30m}") Duration maxDelay){
    this.maxAttempts=maxAttempts;this.baseDelay=baseDelay;this.maxDelay=maxDelay;
  }
  public boolean allows(int attemptNumber){return attemptNumber<=maxAttempts;}
  public Duration delayFor(int attemptNumber,UUID paymentId){
    long multiplier=1L<<Math.min(Math.max(0,attemptNumber-1),20);
    long capped=Math.min(maxDelay.toMillis(),Math.multiplyExact(baseDelay.toMillis(),multiplier));
    long jitter=Math.floorMod(paymentId.getLeastSignificantBits(),Math.max(1,capped/5+1));
    return Duration.ofMillis(Math.min(maxDelay.toMillis(),capped+jitter));
  }
}
