package com.hrutvik.payments.application;

public class RefundRejectedException extends RuntimeException {
  private final String code;
  public RefundRejectedException(String code,String message){super(message);this.code=code;}
  public String getCode(){return code;}
}
