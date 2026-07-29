package com.tenpo.backend_challenge.dto.response;

public record RateLimitErrorResponse(
      int status,
      String message
) {
}
