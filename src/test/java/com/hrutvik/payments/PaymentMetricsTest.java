package com.hrutvik.payments;

import com.hrutvik.payments.domain.ProviderResult;
import com.hrutvik.payments.infrastructure.PaymentMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentMetricsTest {
  @Test void recordsBusinessAndProviderMetrics(){
    var registry=new SimpleMeterRegistry(); var metrics=new PaymentMetrics(registry);
    metrics.created(); metrics.idempotentReplay(); metrics.outcome("SUCCEEDED");
    metrics.providerCall("atlas-pay",()->ProviderResult.success("ref-1"));
    assertThat(registry.counter("payments.created.total").count()).isEqualTo(1);
    assertThat(registry.counter("payments.idempotent.replays.total").count()).isEqualTo(1);
    assertThat(registry.counter("payments.outcomes.total","status","SUCCEEDED").count()).isEqualTo(1);
    assertThat(registry.counter("payments.provider.calls.total","provider","atlas-pay","outcome","SUCCESS").count()).isEqualTo(1);
    assertThat(registry.timer("payments.provider.latency","provider","atlas-pay").count()).isEqualTo(1);
  }
}
