package com.hrutvik.payments;

import com.hrutvik.payments.application.*;
import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.persistence.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefundServiceTest {
  private final PaymentRepository payments=mock(PaymentRepository.class);
  private final RefundRepository refunds=mock(RefundRepository.class);
  private final OutboxRepository outbox=mock(OutboxRepository.class);
  private final RefundService service=new RefundService(payments,refunds,outbox);

  @BeforeEach void setup(){
    reset(payments,refunds,outbox);
    when(refunds.findByIdempotencyKey(any())).thenReturn(Optional.empty());
    when(refunds.save(any())).thenAnswer(i->i.getArgument(0));
    when(outbox.save(any())).thenAnswer(i->i.getArgument(0));
  }

  @Test void createsPartialRefundAndOutboxEvent(){
    Payment payment=successfulPayment(new BigDecimal("100.00"));
    when(payments.lockById(payment.getId())).thenReturn(Optional.of(payment));
    when(refunds.totalSucceededForPayment(payment.getId())).thenReturn(new BigDecimal("25.00"));
    Refund result=service.create(payment.getId(),"refund-001",new BigDecimal("50.00"));
    assertThat(result.getAmount()).isEqualByComparingTo("50.00");
    verify(outbox).save(argThat(e->e.getEventType().equals("REFUND_SUCCEEDED")));
  }

  @Test void rejectsConcurrentOverRefundAgainstLockedBalance(){
    Payment payment=successfulPayment(new BigDecimal("100.00"));
    when(payments.lockById(payment.getId())).thenReturn(Optional.of(payment));
    when(refunds.totalSucceededForPayment(payment.getId())).thenReturn(new BigDecimal("80.00"));
    assertThatThrownBy(()->service.create(payment.getId(),"refund-002",new BigDecimal("25.00")))
        .isInstanceOf(RefundRejectedException.class).hasMessageContaining("remaining");
    verify(refunds,never()).save(any());
  }

  @Test void exactIdempotentReplayReturnsOriginalRefund(){
    UUID paymentId=UUID.randomUUID();
    Refund existing=new Refund(UUID.randomUUID(),paymentId,"refund-003",new BigDecimal("10.00"),"EUR");
    when(refunds.findByIdempotencyKey("refund-003")).thenReturn(Optional.of(existing));
    assertThat(service.create(paymentId,"refund-003",new BigDecimal("10.0"))).isSameAs(existing);
    verifyNoInteractions(payments);verify(outbox,never()).save(any());
  }

  private Payment successfulPayment(BigDecimal amount){
    Payment p=new Payment(UUID.randomUUID(),"payment-key","a".repeat(64),"merchant",amount,"EUR");
    p.succeed("provider-ref");return p;
  }
}
