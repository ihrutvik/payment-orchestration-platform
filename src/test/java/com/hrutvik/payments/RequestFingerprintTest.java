package com.hrutvik.payments;

import com.hrutvik.payments.application.RequestFingerprint;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class RequestFingerprintTest {
  private final RequestFingerprint fingerprints=new RequestFingerprint();
  @Test void normalizesEquivalentAmountsAndCurrencies(){
    assertThat(fingerprints.of("merchant-1",new BigDecimal("42.00"),"eur"))
        .isEqualTo(fingerprints.of("merchant-1",new BigDecimal("42"),"EUR"));
  }
  @Test void changesWhenAChargeDefiningFieldChanges(){
    assertThat(fingerprints.of("merchant-1",new BigDecimal("42"),"EUR"))
        .isNotEqualTo(fingerprints.of("merchant-1",new BigDecimal("43"),"EUR"));
  }
}
