package com.tenpo.backend_challenge.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Pagination(
      int page,
      int size,
      long totalElements,
      int totalPages
) {
}
