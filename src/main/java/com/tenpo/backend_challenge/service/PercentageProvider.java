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

   // TODO pasar a config?
   @Retryable(
         retryFor = PercentageProviderException.class,
         maxAttempts = 3,
         backoff = @Backoff(delay = 500)
   )
   public BigDecimal getPercentage() {
      return FIXED_PERCENTAGE;
   }

   @Recover
   public BigDecimal recover(PercentageProviderException exception) {
      throw new PercentageProviderUnavailableException(
            "Percentage provider is unavailable after 3 attempts",
            exception
      );
   }

}
