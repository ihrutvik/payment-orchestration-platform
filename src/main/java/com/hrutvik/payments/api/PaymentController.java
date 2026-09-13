package com.hrutvik.payments.api;

import com.hrutvik.payments.application.*;
import com.hrutvik.payments.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController @RequestMapping("/v1/payments")
public class PaymentController {
  private final PaymentService service; private final RefundService refundService;
  public PaymentController(PaymentService service,RefundService refundService){this.service=service;this.refundService=refundService;}
  @PostMapping
  ResponseEntity<PaymentResponse> create(@RequestHeader("Idempotency-Key") @Size(min=8,max=100) String key,@Valid @RequestBody CreatePaymentRequest request){
    Payment payment=service.create(key,request.merchantId(),request.amount(),request.currency());
    return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
  }
  @GetMapping("/{id}")
  PaymentResponse get(@PathVariable UUID id){return service.find(id).map(PaymentResponse::from).orElseThrow(()->new PaymentNotFoundException(id));}
  @PostMapping("/{id}/refunds")
  ResponseEntity<RefundResponse> refund(@PathVariable UUID id,@RequestHeader("Idempotency-Key") @Size(min=8,max=100) String key,@Valid @RequestBody CreateRefundRequest request){
    return ResponseEntity.status(HttpStatus.CREATED).body(RefundResponse.from(refundService.create(id,key,request.amount())));
  }
  public record CreatePaymentRequest(@NotBlank String merchantId,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,@NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency){}
  public record CreateRefundRequest(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount){}
  public record RefundResponse(UUID id,UUID paymentId,BigDecimal amount,String currency,String status){
    static RefundResponse from(Refund r){return new RefundResponse(r.getId(),r.getPaymentId(),r.getAmount(),r.getCurrency(),r.getStatus());}
  }
  public record PaymentResponse(UUID id,String merchantId,BigDecimal amount,String currency,String status,String provider,String providerReference,String failureCode,int attempts){
    static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getMerchantId(),p.getAmount(),p.getCurrency(),p.getStatus().name(),p.getProvider(),p.getProviderReference(),p.getFailureCode(),p.getAttempts());}
  }
}
