package com.hrutvik.payments.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="outbox_events")
public class OutboxEvent {
  @Id private UUID id;
  @Column(nullable=false) private String aggregateType;
  @Column(nullable=false) private UUID aggregateId;
  @Column(nullable=false) private String eventType;
  @Column(nullable=false, columnDefinition="text") private String payload;
  @Column(nullable=false) private Instant createdAt;
  private Instant publishedAt;
  protected OutboxEvent() {}
  public OutboxEvent(UUID aggregateId, String eventType, String payload){this.id=UUID.randomUUID();this.aggregateType="PAYMENT";this.aggregateId=aggregateId;this.eventType=eventType;this.payload=payload;this.createdAt=Instant.now();}
  public UUID getId(){return id;} public String getAggregateType(){return aggregateType;} public UUID getAggregateId(){return aggregateId;}
  public String getEventType(){return eventType;} public String getPayload(){return payload;} public Instant getCreatedAt(){return createdAt;} public Instant getPublishedAt(){return publishedAt;}
}
