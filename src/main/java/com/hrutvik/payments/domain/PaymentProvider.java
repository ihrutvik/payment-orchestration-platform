package com.hrutvik.payments.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
  String name();
  ProviderResult charge(UUID paymentId, BigDecimal amount, String currency);
}
