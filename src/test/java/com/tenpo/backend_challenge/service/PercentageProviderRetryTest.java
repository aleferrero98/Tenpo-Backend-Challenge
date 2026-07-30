package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.exception.PercentageProviderException;
import com.tenpo.backend_challenge.exception.PercentageProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(PercentageProviderRetryTest.TestConfig.class)
class PercentageProviderRetryTest {

   private static final AtomicInteger ATTEMPTS = new AtomicInteger();

   @Autowired
   private RetryTestProvider retryTestProvider;

   @BeforeEach
   void setUp() {
      ATTEMPTS.set(0);
   }

   @Test
   void percentageProviderDeclaresRetryAndRecoverConfiguration() throws NoSuchMethodException {
      Method getPercentage = PercentageProvider.class.getMethod("getPercentage");
      Retryable retryable = getPercentage.getAnnotation(Retryable.class);

      assertThat(retryable).isNotNull();
      assertThat(retryable.maxAttempts()).isEqualTo(3);
      assertThat(retryable.backoff().delay()).isEqualTo(500);
      assertThat(retryable.retryFor()).containsExactly(PercentageProviderException.class);
      assertThat(PercentageProvider.class.getMethod("recover", PercentageProviderException.class)
            .isAnnotationPresent(Recover.class)).isTrue();
   }

   @Test
   void retryAttemptsThreeTimesBeforeRecovering() {
      assertThatThrownBy(retryTestProvider::getPercentage)
            .isInstanceOf(PercentageProviderUnavailableException.class)
            .hasMessage("Percentage provider is unavailable after 3 attempts");

      assertThat(ATTEMPTS).hasValue(3);
   }

   @EnableRetry(proxyTargetClass = true)
   @Configuration(proxyBeanMethods = false)
   static class TestConfig {

      @Bean
      RetryTestProvider retryTestProvider() {
         return new RetryTestProvider();
      }
   }

   static class RetryTestProvider {

      @Retryable(retryFor = PercentageProviderException.class, maxAttempts = 3)
      public BigDecimal getPercentage() {
         ATTEMPTS.incrementAndGet();
         throw new PercentageProviderException("External percentage service failed");
      }

      @Recover
      public BigDecimal recover(PercentageProviderException exception) {
         throw new PercentageProviderUnavailableException(
               "Percentage provider is unavailable after 3 attempts",
               exception
         );
      }
   }
}
