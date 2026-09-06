package com.hrutvik.payments.api;

import com.hrutvik.payments.application.WebhookService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/v1/provider-webhooks")
public class WebhookController {
  private final WebhookService service;
  public WebhookController(WebhookService service){this.service=service;}
  @PostMapping
  ResponseEntity<Void> receive(@RequestHeader("X-Provider") String provider,@RequestHeader("X-Event-Id") String eventId,
      @RequestHeader("X-Signature") String signature,@RequestBody String rawPayload){
    return switch(service.receive(provider,eventId,signature,rawPayload)){
      case INVALID_SIGNATURE -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      case INVALID_PAYLOAD -> ResponseEntity.badRequest().build();
      default -> ResponseEntity.accepted().build();
    };
  }
}
