package com.tenpo.backend_challenge.model;

import com.tenpo.backend_challenge.dto.ApiCallHistoryEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "api_call_history")
public class ApiCallHistoryEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "http_method", nullable = false, length = 10)
   private String httpMethod;

   @Column(name = "endpoint", nullable = false, length = 255)
   private String endpoint;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "parameters", columnDefinition = "jsonb")
   private Map<String, Object> parameters;

   @Column(name = "http_status_code", nullable = false)
   private Integer httpStatusCode;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "response_body", columnDefinition = "jsonb")
   private Object responseBody;

   @Column(name = "created_at", nullable = false)
   private OffsetDateTime createdAt;

   public static ApiCallHistoryEntity from(ApiCallHistoryEvent event) {
      ApiCallHistoryEntity entity = new ApiCallHistoryEntity();
      entity.httpMethod = event.httpMethod();
      entity.endpoint = event.endpoint();
      entity.parameters = event.parameters();
      entity.httpStatusCode = event.httpStatusCode();
      entity.responseBody = event.responseBody();
      entity.createdAt = event.createdAt();

      return entity;
   }
}
