package com.hrutvik.payments.api;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import com.hrutvik.payments.application.IdempotencyConflictException;
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(PaymentNotFoundException.class) ResponseEntity<Problem> notFound(PaymentNotFoundException e){return ResponseEntity.status(404).body(new Problem("PAYMENT_NOT_FOUND",e.getMessage(),Instant.now()));}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Problem> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(new Problem("INVALID_REQUEST",e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),Instant.now()));}
  @ExceptionHandler(IdempotencyConflictException.class) ResponseEntity<Problem> conflict(IdempotencyConflictException e){return ResponseEntity.status(409).body(new Problem("IDEMPOTENCY_CONFLICT",e.getMessage(),Instant.now()));}
  public record Problem(String code,String message,Instant timestamp){}
}
