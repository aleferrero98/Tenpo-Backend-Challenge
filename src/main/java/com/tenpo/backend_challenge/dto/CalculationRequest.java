package com.tenpo.backend_challenge.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalculationRequest(
      @NotNull(message = "num1 is required")
      BigDecimal num1,

      @NotNull(message = "num2 is required")
      BigDecimal num2
) {
}
