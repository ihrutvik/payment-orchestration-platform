package com.hrutvik.payments.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WebhookSignatureVerifier {
  private final byte[] secret;
  public WebhookSignatureVerifier(@Value("${payments.webhooks.secret}") String secret){this.secret=secret.getBytes(StandardCharsets.UTF_8);}
  public boolean isValid(String payload,String suppliedHex){
    if(suppliedHex==null || !suppliedHex.matches("[0-9a-fA-F]{64}")) return false;
    try{
      Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret,"HmacSHA256"));
      byte[] expected=mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      return MessageDigest.isEqual(expected,HexFormat.of().parseHex(suppliedHex));
    }catch(Exception e){throw new IllegalStateException("Unable to verify webhook signature",e);}
  }
}
