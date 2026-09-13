package com.hrutvik.payments.persistence;

import com.hrutvik.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);
  Optional<Payment> findByProviderReference(String providerReference);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.id=:id")
  Optional<Payment> lockById(@Param("id") UUID id);
}
