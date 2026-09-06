package com.hrutvik.payments.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrutvik.payments.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {
  private final WebhookSignatureVerifier signatures; private final WebhookEventRepository webhooks;
  private final PaymentRepository payments; private final OutboxRepository outbox; private final ObjectMapper json;
  public WebhookService(WebhookSignatureVerifier signatures,WebhookEventRepository webhooks,PaymentRepository payments,OutboxRepository outbox,ObjectMapper json){
    this.signatures=signatures;this.webhooks=webhooks;this.payments=payments;this.outbox=outbox;this.json=json;
  }
  @Transactional
  public Result receive(String provider,String eventId,String signature,String rawPayload){
    if(!signatures.isValid(rawPayload,signature)) return Result.INVALID_SIGNATURE;
    if(webhooks.existsByProviderAndEventId(provider,eventId)) return Result.DUPLICATE;
    try{
      var body=json.readValue(rawPayload,ProviderWebhook.class);
      webhooks.save(new WebhookEvent(provider,eventId,body.type(),rawPayload));
      var payment=payments.findByProviderReference(body.providerReference()).orElse(null);
      if(payment==null) return Result.ACCEPTED;
      switch(body.type()){
        case "payment.succeeded" -> payment.succeed(body.providerReference());
        case "payment.declined" -> payment.decline(body.failureCode()==null?"PROVIDER_DECLINED":body.failureCode());
        default -> { return Result.ACCEPTED; }
      }
      payments.save(payment);
      outbox.save(new OutboxEvent(payment.getId(),body.type().equals("payment.succeeded")?"PAYMENT_SUCCEEDED":"PAYMENT_DECLINED",rawPayload));
      return Result.PROCESSED;
    }catch(com.fasterxml.jackson.core.JsonProcessingException e){return Result.INVALID_PAYLOAD;}
  }
  public enum Result { PROCESSED, ACCEPTED, DUPLICATE, INVALID_SIGNATURE, INVALID_PAYLOAD }
  public record ProviderWebhook(String type,String providerReference,String failureCode){}
}
