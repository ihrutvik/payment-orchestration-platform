package com.hrutvik.payments.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
  @Query(value="SELECT * FROM outbox_events WHERE published_at IS NULL ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT :batchSize",nativeQuery=true)
  List<OutboxEvent> lockNextBatch(@Param("batchSize") int batchSize);
}
