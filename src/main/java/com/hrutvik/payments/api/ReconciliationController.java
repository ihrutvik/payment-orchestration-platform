package com.hrutvik.payments.api;

import com.hrutvik.payments.application.ReconciliationService;
import com.hrutvik.payments.domain.SettlementRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController @RequestMapping("/v1/reconciliation/records")
public class ReconciliationController {
  private final ReconciliationService service;
  public ReconciliationController(ReconciliationService service){this.service=service;}
  @PostMapping
  ResponseEntity<Response> ingest(@Valid @RequestBody Request request){
    SettlementRecord record=service.reconcile(request.provider(),request.externalRecordId(),request.providerReference(),request.amount(),request.currency());
    return ResponseEntity.status(HttpStatus.CREATED).body(Response.from(record));
  }
  public record Request(@NotBlank @Size(max=100) String provider,@NotBlank @Size(max=255) String externalRecordId,
      @NotBlank @Size(max=255) String providerReference,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,
      @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency){}
  public record Response(UUID id,String provider,String externalRecordId,String providerReference,BigDecimal amount,String currency,String status,UUID paymentId,Instant receivedAt){
    static Response from(SettlementRecord r){return new Response(r.getId(),r.getProvider(),r.getExternalRecordId(),r.getProviderReference(),r.getAmount(),r.getCurrency(),r.getStatus().name(),r.getPaymentId(),r.getReceivedAt());}
  }
}
