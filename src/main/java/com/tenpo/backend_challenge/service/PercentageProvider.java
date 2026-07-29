package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.exception.PercentageProviderException;
import com.tenpo.backend_challenge.exception.PercentageProviderUnavailableException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PercentageProvider {

   private static final BigDecimal FIXED_PERCENTAGE = BigDecimal.TEN;
   private static final int MAX_ATTEMPTS = 3;

   @Retryable(
         retryFor = PercentageProviderException.class,
         maxAttempts = MAX_ATTEMPTS,
         backoff = @Backoff(delay = 500)
   )
   public BigDecimal getPercentage() {
      // This method represents a call to an external service
      return FIXED_PERCENTAGE;
   }

   @Recover
   public BigDecimal recover(PercentageProviderException exception) {
      throw new PercentageProviderUnavailableException(
            "Percentage provider is unavailable after " + MAX_ATTEMPTS + " attempts",
            exception
      );
   }

}
