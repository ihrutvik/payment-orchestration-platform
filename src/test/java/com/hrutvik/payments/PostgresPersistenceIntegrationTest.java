package com.hrutvik.payments;

import com.hrutvik.payments.domain.Payment;
import com.hrutvik.payments.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers(disabledWithoutDocker=true)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class PostgresPersistenceIntegrationTest {
  @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:16-alpine");
  @DynamicPropertySource static void database(DynamicPropertyRegistry registry){
    registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username",POSTGRES::getUsername);
    registry.add("spring.datasource.password",POSTGRES::getPassword);
  }

  @Autowired PaymentRepository payments;
  @Autowired PaymentRetryRepository retries;

  @Test void flywaySchemaEnforcesIdempotencyUniqueness(){
    payments.saveAndFlush(payment("checkout-unique"));
    assertThatThrownBy(()->payments.saveAndFlush(payment("checkout-unique")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test void retryLeaseReturnsOnlyDueUncompletedRows(){
    Payment payment=payments.saveAndFlush(payment("checkout-retry"));
    PaymentRetry due=retries.save(new PaymentRetry(payment.getId(),1,Instant.now().minusSeconds(1),"TIMEOUT"));
    retries.save(new PaymentRetry(payment.getId(),2,Instant.now().plusSeconds(600),"TIMEOUT"));
    var leased=retries.lockDueBatch(Instant.now(),10);
    assertThat(leased).extracting(PaymentRetry::getId).containsExactly(due.getId());
  }

  private Payment payment(String key){
    return new Payment(UUID.randomUUID(),key,"a".repeat(64),"merchant-1",new BigDecimal("42.00"),"EUR");
  }
}
