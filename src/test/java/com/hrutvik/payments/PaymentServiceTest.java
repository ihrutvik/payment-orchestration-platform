package com.hrutvik.payments;

import com.hrutvik.payments.application.PaymentService;
import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.persistence.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
  private final PaymentRepository payments=mock(PaymentRepository.class);
  private final OutboxRepository outbox=mock(OutboxRepository.class);
  private final PaymentProvider primary=mock(PaymentProvider.class);
  private final PaymentProvider fallback=mock(PaymentProvider.class);

  @BeforeEach void setup(){when(payments.save(any())).thenAnswer(i->i.getArgument(0));when(outbox.save(any())).thenAnswer(i->i.getArgument(0));}

  @Test void returnsOriginalPaymentForRepeatedIdempotencyKey(){
    Payment existing=new Payment(UUID.randomUUID(),"checkout-123","merchant",new BigDecimal("42.00"),"EUR");
    when(payments.findByIdempotencyKey("checkout-123")).thenReturn(Optional.of(existing));
    Payment result=service().create("checkout-123","merchant",new BigDecimal("42.00"),"EUR");
    assertThat(result).isSameAs(existing); verifyNoInteractions(primary,fallback); verify(outbox,never()).save(any());
  }

  @Test void routesTransientFailureToFallbackAndWritesSuccessEvent(){
    when(payments.findByIdempotencyKey(any())).thenReturn(Optional.empty());
    when(primary.name()).thenReturn("primary"); when(fallback.name()).thenReturn("fallback");
    when(primary.charge(any(),any(),any())).thenReturn(new ProviderResult(ProviderResult.Outcome.TRANSIENT_ERROR,null,"TIMEOUT"));
    when(fallback.charge(any(),any(),any())).thenReturn(ProviderResult.success("fb_123"));
    Payment result=service().create("checkout-456","merchant",new BigDecimal("26.00"),"usd");
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED); assertThat(result.getAttempts()).isEqualTo(2);
    assertThat(result.getProvider()).isEqualTo("fallback"); assertThat(result.getProviderReference()).isEqualTo("fb_123");
    ArgumentCaptor<OutboxEvent> event=ArgumentCaptor.forClass(OutboxEvent.class); verify(outbox).save(event.capture());
    assertThat(event.getValue().getEventType()).isEqualTo("PAYMENT_SUCCEEDED");
  }

  @Test void neverFallsBackAfterHardDecline(){
    when(payments.findByIdempotencyKey(any())).thenReturn(Optional.empty()); when(primary.name()).thenReturn("primary");
    when(primary.charge(any(),any(),any())).thenReturn(new ProviderResult(ProviderResult.Outcome.HARD_DECLINE,null,"STOLEN_CARD"));
    Payment result=service().create("checkout-789","merchant",new BigDecimal("50.00"),"EUR");
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.DECLINED); verifyNoInteractions(fallback);
  }
  private PaymentService service(){return new PaymentService(payments,outbox,List.of(primary,fallback));}
}
