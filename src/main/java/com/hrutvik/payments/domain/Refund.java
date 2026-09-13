package com.hrutvik.payments.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refunds",uniqueConstraints=@UniqueConstraint(name="uk_refund_idempotency",columnNames="idempotency_key"))
public class Refund {
  @Id private UUID id;
  @Column(name="payment_id",nullable=false,updatable=false) private UUID paymentId;
  @Column(name="idempotency_key",nullable=false,updatable=false,length=100) private String idempotencyKey;
  @Column(nullable=false,precision=19,scale=2,updatable=false) private BigDecimal amount;
  @Column(nullable=false,updatable=false,length=3) private String currency;
  @Column(nullable=false) private String status;
  @Column(nullable=false,updatable=false) private Instant createdAt;

  protected Refund(){}
  public Refund(UUID id,UUID paymentId,String key,BigDecimal amount,String currency){
    this.id=id;this.paymentId=paymentId;this.idempotencyKey=key;this.amount=amount;
    this.currency=currency;this.status="SUCCEEDED";this.createdAt=Instant.now();
  }
  public UUID getId(){return id;} public UUID getPaymentId(){return paymentId;}
  public String getIdempotencyKey(){return idempotencyKey;} public BigDecimal getAmount(){return amount;}
  public String getCurrency(){return currency;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
