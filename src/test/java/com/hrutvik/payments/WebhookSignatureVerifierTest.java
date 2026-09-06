package com.hrutvik.payments;

import com.hrutvik.payments.application.WebhookSignatureVerifier;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureVerifierTest {
  private final WebhookSignatureVerifier verifier=new WebhookSignatureVerifier("test-secret");
  @Test void acceptsValidSignature(){
    assertThat(verifier.isValid("{\"ok\":true}","97b4f11584439784b801821ece72af4e29c19573aacf94bcc0533c09086b3643")).isTrue();
  }
  @Test void rejectsTamperedPayloadAndMalformedSignature(){
    assertThat(verifier.isValid("{\"ok\":false}","97b4f11584439784b801821ece72af4e29c19573aacf94bcc0533c09086b3643")).isFalse();
    assertThat(verifier.isValid("payload","not-hex")).isFalse();
  }
}
