package com.hrutvik.payments.infrastructure;

import com.hrutvik.payments.application.PaymentService;
import com.hrutvik.payments.persistence.PaymentRetryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class PaymentRetryWorker {
  private final PaymentRetryRepository retries; private final PaymentService payments; private final int batchSize;
  public PaymentRetryWorker(PaymentRetryRepository retries,PaymentService payments,
      @Value("${payments.retry.batch-size:50}") int batchSize){this.retries=retries;this.payments=payments;this.batchSize=batchSize;}
  @Scheduled(fixedDelayString="${payments.retry.poll-delay:5s}")
  @Transactional
  public void processDueRetries(){
    for(var retry:retries.lockDueBatch(Instant.now(),batchSize)){
      payments.retry(retry.getPaymentId(),retry.getAttemptNumber());
      retry.complete();
    }
  }
}
