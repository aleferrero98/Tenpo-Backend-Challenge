package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.exception.PercentageProviderUnavailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicPercentageServiceTest {

   private final PercentageProvider percentageProvider = mock(PercentageProvider.class);
   private final DynamicPercentageService service = new DynamicPercentageService(percentageProvider);

   @Test
   void calculateAppliesDynamicPercentageCorrectly() {
      when(percentageProvider.getPercentage()).thenReturn(BigDecimal.TEN);

      BigDecimal result = service.calculate(BigDecimal.valueOf(5), BigDecimal.valueOf(5));

      assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11));
   }

   @Test
   void calculateSupportsDecimalValues() {
      when(percentageProvider.getPercentage()).thenReturn(BigDecimal.TEN);

      BigDecimal result = service.calculate(BigDecimal.valueOf(10.50), BigDecimal.valueOf(5.25));

      assertThat(result).isEqualByComparingTo(new BigDecimal("17.325"));
   }

   @Test
   void calculatePropagatesProviderUnavailableError() {
      PercentageProviderUnavailableException exception = new PercentageProviderUnavailableException(
            "Percentage provider is unavailable after 3 attempts",
            new RuntimeException("Provider failure")
      );
      when(percentageProvider.getPercentage()).thenThrow(exception);

      assertThatThrownBy(() -> service.calculate(BigDecimal.ONE, BigDecimal.ONE))
            .isSameAs(exception);
   }
}
