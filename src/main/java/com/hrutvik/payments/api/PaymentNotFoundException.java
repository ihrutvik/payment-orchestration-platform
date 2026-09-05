package com.hrutvik.payments.api;
import java.util.UUID;
public class PaymentNotFoundException extends RuntimeException { public PaymentNotFoundException(UUID id){super("Payment not found: "+id);} }
