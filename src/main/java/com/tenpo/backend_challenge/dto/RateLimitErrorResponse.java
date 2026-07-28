package com.tenpo.backend_challenge.dto;

public record RateLimitErrorResponse(
      int status,
      String message
) {
}
