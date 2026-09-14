package com.hrutvik.payments.application;

import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.infrastructure.PaymentMetrics;
import com.hrutvik.payments.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ReconciliationService {
  private final SettlementRecordRepository records; private final PaymentRepository payments;
  private final OutboxRepository outbox; private final PaymentMetrics metrics;
  public ReconciliationService(SettlementRecordRepository records,PaymentRepository payments,OutboxRepository outbox,PaymentMetrics metrics){
    this.records=records;this.payments=payments;this.outbox=outbox;this.metrics=metrics;
  }

  @Transactional
  public SettlementRecord reconcile(String provider,String externalId,String reference,BigDecimal amount,String currency){
    var existing=records.findByProviderAndExternalRecordId(provider,externalId);
    if(existing.isPresent()){
      if(!existing.get().sameRequest(reference,amount,currency)) throw new IdempotencyConflictException();
      return existing.get();
    }
    Payment payment=payments.findByProviderReference(reference).orElse(null);
    ReconciliationStatus status=classify(payment,amount,currency);
    SettlementRecord record=records.save(new SettlementRecord(UUID.randomUUID(),provider,externalId,reference,amount,currency.toUpperCase(Locale.ROOT),status,payment==null?null:payment.getId()));
    metrics.reconciliation(status.name());
    if(status!=ReconciliationStatus.MATCHED){
      outbox.save(new OutboxEvent("RECONCILIATION",record.getId(),"SETTLEMENT_DISCREPANCY_DETECTED",
          "{\"recordId\":\""+record.getId()+"\",\"providerReference\":\""+reference+"\",\"reason\":\""+status+"\"}"));
    }
    return record;
  }

  private ReconciliationStatus classify(Payment payment,BigDecimal amount,String currency){
    if(payment==null) return ReconciliationStatus.PAYMENT_NOT_FOUND;
    if(payment.getAmount().compareTo(amount)!=0) return ReconciliationStatus.AMOUNT_MISMATCH;
    if(!payment.getCurrency().equalsIgnoreCase(currency)) return ReconciliationStatus.CURRENCY_MISMATCH;
    return ReconciliationStatus.MATCHED;
  }
}
