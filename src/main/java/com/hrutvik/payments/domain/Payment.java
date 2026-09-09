package com.hrutvik.payments.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uk_payment_idempotency", columnNames = "idempotency_key"))
public class Payment {
  @Id private UUID id;
  @Column(name="idempotency_key", nullable=false, updatable=false) private String idempotencyKey;
  @Column(name="request_hash",length=64,updatable=false) private String requestHash;
  @Column(nullable=false, updatable=false) private String merchantId;
  @Column(nullable=false, precision=19, scale=2, updatable=false) private BigDecimal amount;
  @Column(nullable=false, length=3, updatable=false) private String currency;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private PaymentStatus status;
  private String provider;
  private String providerReference;
  private String failureCode;
  @Column(nullable=false) private int attempts;
  @Column(nullable=false, updatable=false) private Instant createdAt;
  @Column(nullable=false) private Instant updatedAt;
  @Version private long version;

  protected Payment() {}
  public Payment(UUID id,String key,String requestHash,String merchantId,BigDecimal amount,String currency) {
    this.id=id;this.idempotencyKey=key;this.requestHash=requestHash;this.merchantId=merchantId;this.amount=amount;
    this.currency=currency; this.status=PaymentStatus.PROCESSING; this.createdAt=Instant.now(); this.updatedAt=this.createdAt;
  }
  public void recordAttempt(String provider) { this.provider=provider; this.attempts++; touch(); }
  public void succeed(String reference) { status=PaymentStatus.SUCCEEDED; providerReference=reference; failureCode=null; touch(); }
  public void decline(String code) { status=PaymentStatus.DECLINED; failureCode=code; touch(); }
  public void retry(String code) { status=PaymentStatus.RETRY_SCHEDULED; failureCode=code; touch(); }
  public void fail(String code) { status=PaymentStatus.FAILED; failureCode=code; touch(); }
  private void touch(){ updatedAt=Instant.now(); }
  public UUID getId(){return id;} public String getIdempotencyKey(){return idempotencyKey;} public String getRequestHash(){return requestHash;} public String getMerchantId(){return merchantId;}
  public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public PaymentStatus getStatus(){return status;}
  public String getProvider(){return provider;} public String getProviderReference(){return providerReference;} public String getFailureCode(){return failureCode;}
  public int getAttempts(){return attempts;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
