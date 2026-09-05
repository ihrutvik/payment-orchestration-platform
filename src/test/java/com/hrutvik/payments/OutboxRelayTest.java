package com.hrutvik.payments;

import com.hrutvik.payments.application.EventPublisher;
import com.hrutvik.payments.infrastructure.OutboxRelay;
import com.hrutvik.payments.persistence.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxRelayTest {
  @Test void marksAnEventPublishedOnlyAfterKafkaAcceptsIt(){
    OutboxRepository repository=mock(OutboxRepository.class);
    EventPublisher publisher=mock(EventPublisher.class);
    OutboxEvent event=new OutboxEvent(UUID.randomUUID(),"PAYMENT_SUCCEEDED","{\"status\":\"SUCCEEDED\"}");
    when(repository.lockNextBatch(25)).thenReturn(List.of(event));
    new OutboxRelay(repository,publisher,25).publishBatch();
    verify(publisher).publish(event.getId(),event.getAggregateId(),event.getEventType(),event.getPayload());
    assertThat(event.getPublishedAt()).isNotNull();
  }

  @Test void leavesEventUnpublishedWhenKafkaFails(){
    OutboxRepository repository=mock(OutboxRepository.class);
    EventPublisher publisher=mock(EventPublisher.class);
    OutboxEvent event=new OutboxEvent(UUID.randomUUID(),"PAYMENT_FAILED","{}");
    when(repository.lockNextBatch(25)).thenReturn(List.of(event));
    doThrow(new RuntimeException("broker unavailable")).when(publisher).publish(any(),any(),any(),any());
    try { new OutboxRelay(repository,publisher,25).publishBatch(); } catch(RuntimeException ignored) {}
    assertThat(event.getPublishedAt()).isNull();
  }
}
