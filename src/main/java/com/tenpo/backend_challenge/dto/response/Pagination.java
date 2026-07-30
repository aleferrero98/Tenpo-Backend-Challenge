package com.tenpo.backend_challenge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pagination(
      int page,

      int size,

      @JsonProperty("total_elements")
      long totalElements,

      @JsonProperty("total_pages")
      int totalPages
) {
}
