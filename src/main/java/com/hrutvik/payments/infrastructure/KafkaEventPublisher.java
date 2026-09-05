package com.hrutvik.payments.infrastructure;

import com.hrutvik.payments.application.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.UUID;

@Component
public class KafkaEventPublisher implements EventPublisher {
  private final KafkaTemplate<String,String> kafka;
  private final String topic;
  private final Duration timeout;

  public KafkaEventPublisher(KafkaTemplate<String,String> kafka,
      @Value("${payments.events.topic:payment-events}") String topic,
      @Value("${payments.events.publish-timeout:5s}") Duration timeout){
    this.kafka=kafka; this.topic=topic; this.timeout=timeout;
  }

  @Override
  public void publish(UUID eventId, UUID aggregateId, String eventType, String payload){
    kafka.send(topic,aggregateId.toString(),payload)
        .orTimeout(timeout.toMillis(),java.util.concurrent.TimeUnit.MILLISECONDS)
        .join();
  }
}
