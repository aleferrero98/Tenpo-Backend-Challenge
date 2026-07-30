package com.tenpo.backend_challenge.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiCallHistoryResponse(
      @JsonProperty("http_method")
      String httpMethod,

      String endpoint,

      Map<String, Object> parameters,

      @JsonProperty("http_status_code")
      Integer httpStatusCode,

      Object response,

      @JsonProperty("created_at")
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
      OffsetDateTime createdAt
) {
}
