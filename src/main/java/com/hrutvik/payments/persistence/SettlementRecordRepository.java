package com.hrutvik.payments.persistence;

import com.hrutvik.payments.domain.SettlementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SettlementRecordRepository extends JpaRepository<SettlementRecord,UUID> {
  Optional<SettlementRecord> findByProviderAndExternalRecordId(String provider,String externalRecordId);
}
