package com.hrutvik.payments.infrastructure;

import com.hrutvik.payments.domain.ProviderResult;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;

@Component
public class PaymentMetrics {
  private final MeterRegistry registry;
  public PaymentMetrics(MeterRegistry registry){this.registry=registry;}

  public void created(){registry.counter("payments.created.total").increment();}
  public void idempotentReplay(){registry.counter("payments.idempotent.replays.total").increment();}
  public void outcome(String status){registry.counter("payments.outcomes.total","status",status).increment();}
  public void providerCircuitOpen(String provider){registry.counter("payments.provider.circuit.open.total","provider",provider).increment();}
  public ProviderResult providerCall(String provider,Supplier<ProviderResult> call){
    Timer.Sample sample=Timer.start(registry);
    try{
      ProviderResult result=call.get();
      registry.counter("payments.provider.calls.total","provider",provider,"outcome",result.outcome().name()).increment();
      return result;
    }catch(RuntimeException e){
      registry.counter("payments.provider.calls.total","provider",provider,"outcome","EXCEPTION").increment();
      throw e;
    }finally{
      sample.stop(Timer.builder("payments.provider.latency").tag("provider",provider).publishPercentileHistogram().register(registry));
    }
  }
}
