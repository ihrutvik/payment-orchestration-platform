package com.hrutvik.payments.application;

import com.hrutvik.payments.domain.ProviderResult;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;

class ProviderCircuitBreakerTest {
  @Test void opensAfterThresholdAndAllowsOneRecoveryProbe(){
    MutableClock clock=new MutableClock(Instant.parse("2026-09-12T00:00:00Z"));
    ProviderCircuitBreaker breaker=new ProviderCircuitBreaker(2,Duration.ofSeconds(30),clock);

    breaker.record("atlas",ProviderResult.Outcome.TRANSIENT_ERROR);
    assertThat(breaker.allowRequest("atlas")).isTrue();
    breaker.record("atlas",ProviderResult.Outcome.TRANSIENT_ERROR);
    assertThat(breaker.allowRequest("atlas")).isFalse();

    clock.advance(Duration.ofSeconds(31));
    assertThat(breaker.allowRequest("atlas")).isTrue();
    assertThat(breaker.allowRequest("atlas")).isFalse();
    breaker.record("atlas",ProviderResult.Outcome.SUCCESS);
    assertThat(breaker.allowRequest("atlas")).isTrue();
  }

  @Test void businessDeclinesDoNotTripInfrastructureCircuit(){
    ProviderCircuitBreaker breaker=new ProviderCircuitBreaker(1,Duration.ofMinutes(1),Clock.systemUTC());
    breaker.record("atlas",ProviderResult.Outcome.HARD_DECLINE);
    assertThat(breaker.allowRequest("atlas")).isTrue();
  }

  private static final class MutableClock extends Clock {
    private Instant instant;
    private MutableClock(Instant instant){this.instant=instant;}
    void advance(Duration duration){instant=instant.plus(duration);}
    public ZoneId getZone(){return ZoneOffset.UTC;}
    public Clock withZone(ZoneId zone){return this;}
    public Instant instant(){return instant;}
  }
}
