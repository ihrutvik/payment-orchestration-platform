package com.hrutvik.payments.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="webhook_events",uniqueConstraints=@UniqueConstraint(name="uk_webhook_provider_event",columnNames={"provider","event_id"}))
public class WebhookEvent {
  @Id private UUID id;
  @Column(nullable=false,updatable=false) private String provider;
  @Column(name="event_id",nullable=false,updatable=false) private String eventId;
  @Column(nullable=false,updatable=false) private String eventType;
  @Column(nullable=false,updatable=false,columnDefinition="text") private String payload;
  @Column(nullable=false,updatable=false) private Instant receivedAt;
  protected WebhookEvent(){}
  public WebhookEvent(String provider,String eventId,String eventType,String payload){
    this.id=UUID.randomUUID();this.provider=provider;this.eventId=eventId;this.eventType=eventType;this.payload=payload;this.receivedAt=Instant.now();
  }
  public UUID getId(){return id;} public String getProvider(){return provider;} public String getEventId(){return eventId;}
  public String getEventType(){return eventType;} public String getPayload(){return payload;} public Instant getReceivedAt(){return receivedAt;}
}
