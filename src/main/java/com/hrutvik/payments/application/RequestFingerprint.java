package com.hrutvik.payments.application;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class RequestFingerprint {
  public String of(String merchantId,BigDecimal amount,String currency){
    String canonical=merchantId.trim()+"\n"+amount.stripTrailingZeros().toPlainString()+"\n"+currency.toUpperCase(Locale.ROOT);
    try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}
    catch(Exception e){throw new IllegalStateException("SHA-256 is unavailable",e);}
  }
}
