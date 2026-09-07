package com.hrutvik.payments.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="payment_retries")
public class PaymentRetry {
  @Id private UUID id;
  @Column(nullable=false,updatable=false) private UUID paymentId;
  @Column(nullable=false,updatable=false) private int attemptNumber;
  @Column(nullable=false,updatable=false) private Instant nextAttemptAt;
  @Column(nullable=false,updatable=false) private String reason;
  @Column(nullable=false) private boolean completed;
  @Column(nullable=false,updatable=false) private Instant createdAt;
  private Instant completedAt;
  protected PaymentRetry(){}
  public PaymentRetry(UUID paymentId,int attemptNumber,Instant nextAttemptAt,String reason){
    this.id=UUID.randomUUID();this.paymentId=paymentId;this.attemptNumber=attemptNumber;
    this.nextAttemptAt=nextAttemptAt;this.reason=reason;this.createdAt=Instant.now();
  }
  public void complete(){this.completed=true;this.completedAt=Instant.now();}
  public UUID getId(){return id;} public UUID getPaymentId(){return paymentId;} public int getAttemptNumber(){return attemptNumber;}
  public Instant getNextAttemptAt(){return nextAttemptAt;} public String getReason(){return reason;} public boolean isCompleted(){return completed;}
  public Instant getCreatedAt(){return createdAt;} public Instant getCompletedAt(){return completedAt;}
}
