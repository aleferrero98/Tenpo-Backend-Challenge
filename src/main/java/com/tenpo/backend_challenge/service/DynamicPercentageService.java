package com.tenpo.backend_challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
@RequiredArgsConstructor
public class DynamicPercentageService {

   private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

   private final PercentageProvider percentageProvider;

   public BigDecimal calculate(BigDecimal num1, BigDecimal num2) {
      BigDecimal sum = num1.add(num2);
      BigDecimal percentage = percentageProvider.getPercentage();
      BigDecimal additionalAmount = sum.multiply(percentage).divide(ONE_HUNDRED, MathContext.DECIMAL64);

      return sum.add(additionalAmount);
   }
}
