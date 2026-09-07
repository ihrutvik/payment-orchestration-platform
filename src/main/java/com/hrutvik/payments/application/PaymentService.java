package com.hrutvik.payments.application;

import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentService {
  private final PaymentRepository payments; private final OutboxRepository outbox; private final PaymentRetryRepository retries;
  private final RetryPolicy retryPolicy; private final List<PaymentProvider> providers;
  public PaymentService(PaymentRepository payments,OutboxRepository outbox,PaymentRetryRepository retries,RetryPolicy retryPolicy,List<PaymentProvider> providers){
    this.payments=payments;this.outbox=outbox;this.retries=retries;this.retryPolicy=retryPolicy;this.providers=providers;
  }

  @Transactional
  public Payment create(String key, String merchantId, BigDecimal amount, String currency){
    var existing=payments.findByIdempotencyKey(key);
    if(existing.isPresent()) return existing.get();
    var payment=payments.save(new Payment(UUID.randomUUID(),key,merchantId,amount,currency.toUpperCase(Locale.ROOT)));
    return attempt(payment,0);
  }

  @Transactional(readOnly=true)
  public Optional<Payment> find(UUID id){ return payments.findById(id); }

  @Transactional
  public void retry(UUID paymentId,int attemptNumber){
    var payment=payments.findById(paymentId).orElseThrow();
    if(payment.getStatus()==PaymentStatus.SUCCEEDED || payment.getStatus()==PaymentStatus.DECLINED) return;
    attempt(payment,attemptNumber);
  }

  private Payment attempt(Payment payment,int retryNumber){
    for(var provider:providers){
      payment.recordAttempt(provider.name());
      ProviderResult result=provider.charge(payment.getId(), payment.getAmount(), payment.getCurrency());
      switch(result.outcome()){
        case SUCCESS -> { payment.succeed(result.reference()); return complete(payment,"PAYMENT_SUCCEEDED"); }
        case HARD_DECLINE -> { payment.decline(result.code()); return complete(payment,"PAYMENT_DECLINED"); }
        case SOFT_DECLINE, TRANSIENT_ERROR -> payment.retry(result.code());
      }
    }
    int nextRetry=retryNumber+1;
    if(retryPolicy.allows(nextRetry)){
      payment.retry("PROVIDERS_EXHAUSTED");
      payments.save(payment);
      var nextAt=java.time.Instant.now().plus(retryPolicy.delayFor(nextRetry,payment.getId()));
      retries.save(new PaymentRetry(payment.getId(),nextRetry,nextAt,"PROVIDERS_EXHAUSTED"));
      outbox.save(new OutboxEvent(payment.getId(),"PAYMENT_RETRY_SCHEDULED","{\"paymentId\":\""+payment.getId()+"\",\"attempt\":"+nextRetry+",\"nextAttemptAt\":\""+nextAt+"\"}"));
      return payment;
    }
    payment.fail("RETRY_LIMIT_EXHAUSTED");
    return complete(payment,"PAYMENT_FAILED");
  }

  private Payment complete(Payment payment,String event){
    var saved=payments.save(payment);
    outbox.save(new OutboxEvent(saved.getId(),event,"{\"paymentId\":\""+saved.getId()+"\",\"status\":\""+saved.getStatus()+"\"}"));
    return saved;
  }
}
