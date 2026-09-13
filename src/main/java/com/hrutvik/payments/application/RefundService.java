package com.hrutvik.payments.application;

import com.hrutvik.payments.api.PaymentNotFoundException;
import com.hrutvik.payments.domain.*;
import com.hrutvik.payments.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RefundService {
  private final PaymentRepository payments; private final RefundRepository refunds; private final OutboxRepository outbox;
  public RefundService(PaymentRepository payments,RefundRepository refunds,OutboxRepository outbox){
    this.payments=payments;this.refunds=refunds;this.outbox=outbox;
  }

  @Transactional
  public Refund create(UUID paymentId,String key,BigDecimal amount){
    var existing=refunds.findByIdempotencyKey(key);
    if(existing.isPresent()){
      Refund refund=existing.get();
      if(!refund.getPaymentId().equals(paymentId) || refund.getAmount().compareTo(amount)!=0)
        throw new IdempotencyConflictException();
      return refund;
    }
    Payment payment=payments.lockById(paymentId).orElseThrow(()->new PaymentNotFoundException(paymentId));
    if(payment.getStatus()!=PaymentStatus.SUCCEEDED)
      throw new RefundRejectedException("PAYMENT_NOT_REFUNDABLE","Only successful payments can be refunded");
    BigDecimal remaining=payment.getAmount().subtract(refunds.totalSucceededForPayment(paymentId));
    if(amount.compareTo(remaining)>0)
      throw new RefundRejectedException("REFUND_AMOUNT_EXCEEDED","Refund exceeds the remaining refundable amount");
    Refund refund=refunds.save(new Refund(UUID.randomUUID(),paymentId,key,amount,payment.getCurrency()));
    outbox.save(new OutboxEvent(paymentId,"REFUND_SUCCEEDED","{\"paymentId\":\""+paymentId+"\",\"refundId\":\""+refund.getId()+"\",\"amount\":"+amount+"}"));
    return refund;
  }
}
