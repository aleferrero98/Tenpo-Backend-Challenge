package com.tenpo.backend_challenge.dto;

import java.time.Instant;

public record RateLimitState(
      int requestCount,
      Instant windowStart
) {
}
