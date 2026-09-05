package com.hrutvik.payments.infrastructure;

import com.hrutvik.payments.application.EventPublisher;
import com.hrutvik.payments.persistence.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelay {
  private final OutboxRepository outbox;
  private final EventPublisher publisher;
  private final int batchSize;

  public OutboxRelay(OutboxRepository outbox,EventPublisher publisher,
      @Value("${payments.outbox.batch-size:100}") int batchSize){
    this.outbox=outbox; this.publisher=publisher; this.batchSize=batchSize;
  }

  @Scheduled(fixedDelayString="${payments.outbox.poll-delay:1s}")
  @Transactional
  public void publishBatch(){
    for(var event:outbox.lockNextBatch(batchSize)){
      publisher.publish(event.getId(),event.getAggregateId(),event.getEventType(),event.getPayload());
      event.markPublished();
    }
  }
}
