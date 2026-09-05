package com.hrutvik.payments.infrastructure;
import com.hrutvik.payments.domain.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;
@Component @Order(2)
public class FallbackPaymentProvider implements PaymentProvider {
  public String name(){return "nova-pay";}
  public ProviderResult charge(UUID id, BigDecimal amount, String currency){return ProviderResult.success("nova_"+id.toString().substring(0,12));}
}
