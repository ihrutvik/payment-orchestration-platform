package com.hrutvik.payments.infrastructure;

import com.hrutvik.payments.domain.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component @Order(1)
public class DeterministicPaymentProvider implements PaymentProvider {
  public String name(){return "atlas-pay";}
  public ProviderResult charge(UUID id, BigDecimal amount, String currency){
    if(amount.compareTo(new BigDecimal("9999"))>0) return new ProviderResult(ProviderResult.Outcome.HARD_DECLINE,null,"LIMIT_EXCEEDED");
    if(amount.remainder(new BigDecimal("13")).signum()==0) return new ProviderResult(ProviderResult.Outcome.TRANSIENT_ERROR,null,"PROVIDER_TIMEOUT");
    return ProviderResult.success("atlas_"+id.toString().substring(0,12));
  }
}
