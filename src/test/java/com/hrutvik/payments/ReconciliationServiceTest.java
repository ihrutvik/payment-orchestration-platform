package com.hrutvik.payments;

import com.hrutvik.payments.application.*;
import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.infrastructure.PaymentMetrics;
import com.hrutvik.payments.persistence.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReconciliationServiceTest {
  private final SettlementRecordRepository records=mock(SettlementRecordRepository.class);
  private final PaymentRepository payments=mock(PaymentRepository.class);
  private final OutboxRepository outbox=mock(OutboxRepository.class);
  private final ReconciliationService service=new ReconciliationService(records,payments,outbox,new PaymentMetrics(new SimpleMeterRegistry()));
  @BeforeEach void setup(){reset(records,payments,outbox);when(records.save(any())).thenAnswer(i->i.getArgument(0));when(outbox.save(any())).thenAnswer(i->i.getArgument(0));}

  @Test void matchesProviderSettlementToPayment(){
    Payment payment=payment("ref-1",new BigDecimal("42.00"),"EUR");
    when(payments.findByProviderReference("ref-1")).thenReturn(Optional.of(payment));
    SettlementRecord result=service.reconcile("atlas","daily-1","ref-1",new BigDecimal("42.0"),"eur");
    assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.MATCHED);
    assertThat(result.getPaymentId()).isEqualTo(payment.getId());
    verify(outbox,never()).save(any());
  }

  @Test void emitsAlertEventForAmountMismatch(){
    when(payments.findByProviderReference("ref-2")).thenReturn(Optional.of(payment("ref-2",new BigDecimal("42.00"),"EUR")));
    SettlementRecord result=service.reconcile("atlas","daily-2","ref-2",new BigDecimal("41.00"),"EUR");
    assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.AMOUNT_MISMATCH);
    verify(outbox).save(argThat(e->e.getEventType().equals("SETTLEMENT_DISCREPANCY_DETECTED")));
  }

  @Test void rejectsChangedDuplicateSettlementRecord(){
    SettlementRecord existing=new SettlementRecord(UUID.randomUUID(),"atlas","daily-3","ref-3",new BigDecimal("10.00"),"EUR",ReconciliationStatus.MATCHED,UUID.randomUUID());
    when(records.findByProviderAndExternalRecordId("atlas","daily-3")).thenReturn(Optional.of(existing));
    assertThatThrownBy(()->service.reconcile("atlas","daily-3","ref-3",new BigDecimal("11.00"),"EUR"))
        .isInstanceOf(IdempotencyConflictException.class);
  }

  private Payment payment(String reference,BigDecimal amount,String currency){
    Payment payment=new Payment(UUID.randomUUID(),"payment-"+reference,"a".repeat(64),"merchant",amount,currency);
    payment.succeed(reference);return payment;
  }
}
