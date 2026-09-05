package com.hrutvik.payments.api;

import com.hrutvik.payments.application.PaymentService;
import com.hrutvik.payments.domain.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController @RequestMapping("/v1/payments")
public class PaymentController {
  private final PaymentService service;
  public PaymentController(PaymentService service){this.service=service;}
  @PostMapping
  ResponseEntity<PaymentResponse> create(@RequestHeader("Idempotency-Key") @Size(min=8,max=100) String key,@Valid @RequestBody CreatePaymentRequest request){
    Payment payment=service.create(key,request.merchantId(),request.amount(),request.currency());
    return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
  }
  @GetMapping("/{id}")
  PaymentResponse get(@PathVariable UUID id){return service.find(id).map(PaymentResponse::from).orElseThrow(()->new PaymentNotFoundException(id));}
  public record CreatePaymentRequest(@NotBlank String merchantId,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,@NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency){}
  public record PaymentResponse(UUID id,String merchantId,BigDecimal amount,String currency,String status,String provider,String providerReference,String failureCode,int attempts){
    static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getMerchantId(),p.getAmount(),p.getCurrency(),p.getStatus().name(),p.getProvider(),p.getProviderReference(),p.getFailureCode(),p.getAttempts());}
  }
}
