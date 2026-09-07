package com.hrutvik.payments.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface PaymentRetryRepository extends JpaRepository<PaymentRetry,UUID>{
  @Query(value="SELECT * FROM payment_retries WHERE completed = false AND next_attempt_at <= :now ORDER BY next_attempt_at FOR UPDATE SKIP LOCKED LIMIT :batchSize",nativeQuery=true)
  List<PaymentRetry> lockDueBatch(@Param("now") Instant now,@Param("batchSize") int batchSize);
}
