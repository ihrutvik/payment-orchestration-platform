package com.hrutvik.payments.persistence;

import com.hrutvik.payments.domain.Refund;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;

public interface RefundRepository extends JpaRepository<Refund,UUID> {
  Optional<Refund> findByIdempotencyKey(String idempotencyKey);
  @Query("select coalesce(sum(r.amount),0) from Refund r where r.paymentId=:paymentId and r.status='SUCCEEDED'")
  BigDecimal totalSucceededForPayment(@Param("paymentId") UUID paymentId);
}
