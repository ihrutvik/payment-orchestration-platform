package com.hrutvik.payments;

import com.hrutvik.payments.application.RetryPolicy;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyTest {
  private final UUID paymentId=UUID.fromString("00000000-0000-0000-0000-000000000007");
  private final RetryPolicy policy=new RetryPolicy(5,Duration.ofSeconds(30),Duration.ofMinutes(5));
  @Test void growsExponentiallyAndCapsTheDelay(){
    assertThat(policy.delayFor(1,paymentId)).isBetween(Duration.ofSeconds(30),Duration.ofSeconds(36));
    assertThat(policy.delayFor(2,paymentId)).isGreaterThanOrEqualTo(Duration.ofSeconds(60));
    assertThat(policy.delayFor(5,paymentId)).isLessThanOrEqualTo(Duration.ofMinutes(5));
  }
  @Test void stopsAfterConfiguredAttemptLimit(){
    assertThat(policy.allows(5)).isTrue(); assertThat(policy.allows(6)).isFalse();
  }
}
