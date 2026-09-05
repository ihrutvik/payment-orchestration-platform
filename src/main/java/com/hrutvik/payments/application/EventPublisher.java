package com.hrutvik.payments.application;

import java.util.UUID;

public interface EventPublisher {
  void publish(UUID eventId, UUID aggregateId, String eventType, String payload);
}
