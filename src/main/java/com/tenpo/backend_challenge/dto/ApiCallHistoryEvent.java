package com.tenpo.backend_challenge.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiCallHistoryEvent(
      String httpMethod,
      String endpoint,
      Map<String, Object> parameters,
      int httpStatusCode,
      Object responseBody,
      OffsetDateTime createdAt
) {
}
