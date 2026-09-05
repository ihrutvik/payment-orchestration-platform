package com.hrutvik.payments.domain;

public record ProviderResult(Outcome outcome, String reference, String code) {
  public enum Outcome { SUCCESS, SOFT_DECLINE, HARD_DECLINE, TRANSIENT_ERROR }
  public static ProviderResult success(String ref){ return new ProviderResult(Outcome.SUCCESS, ref, null); }
}
