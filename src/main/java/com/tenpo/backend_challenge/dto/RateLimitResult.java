package com.tenpo.backend_challenge.dto;

public record RateLimitResult(
      boolean allowed,
      long retryAfterSeconds
) {
}
