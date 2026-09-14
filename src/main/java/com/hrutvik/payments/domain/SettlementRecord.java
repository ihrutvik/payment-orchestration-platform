package com.hrutvik.payments.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="settlement_records",uniqueConstraints=@UniqueConstraint(name="uk_settlement_provider_record",columnNames={"provider","external_record_id"}))
public class SettlementRecord {
  @Id private UUID id;
  @Column(nullable=false,updatable=false) private String provider;
  @Column(name="external_record_id",nullable=false,updatable=false) private String externalRecordId;
  @Column(name="provider_reference",nullable=false,updatable=false) private String providerReference;
  @Column(nullable=false,precision=19,scale=2,updatable=false) private BigDecimal amount;
  @Column(nullable=false,length=3,updatable=false) private String currency;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private ReconciliationStatus status;
  @Column(name="payment_id") private UUID paymentId;
  @Column(nullable=false,updatable=false) private Instant receivedAt;

  protected SettlementRecord(){}
  public SettlementRecord(UUID id,String provider,String externalRecordId,String providerReference,BigDecimal amount,String currency,ReconciliationStatus status,UUID paymentId){
    this.id=id;this.provider=provider;this.externalRecordId=externalRecordId;this.providerReference=providerReference;
    this.amount=amount;this.currency=currency;this.status=status;this.paymentId=paymentId;this.receivedAt=Instant.now();
  }
  public boolean sameRequest(String reference,BigDecimal candidateAmount,String candidateCurrency){
    return providerReference.equals(reference)&&amount.compareTo(candidateAmount)==0&&currency.equalsIgnoreCase(candidateCurrency);
  }
  public UUID getId(){return id;} public String getProvider(){return provider;} public String getExternalRecordId(){return externalRecordId;}
  public String getProviderReference(){return providerReference;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;}
  public ReconciliationStatus getStatus(){return status;} public UUID getPaymentId(){return paymentId;} public Instant getReceivedAt(){return receivedAt;}
}
