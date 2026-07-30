package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.dto.ApiCallHistoryEvent;
import com.tenpo.backend_challenge.dto.response.ApiCallHistoryResponse;
import com.tenpo.backend_challenge.dto.response.PagedResponse;
import com.tenpo.backend_challenge.exception.InvalidPageRequestException;
import com.tenpo.backend_challenge.exception.InvalidSortFieldException;
import com.tenpo.backend_challenge.model.ApiCallHistoryEntity;
import com.tenpo.backend_challenge.repository.ApiCallHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiCallHistoryServiceTest {

   private final ApiCallHistoryRepository repository = mock(ApiCallHistoryRepository.class);
   private final ApiCallHistoryService service = new ApiCallHistoryService(repository);

   @Test
   void findAllReturnsPagedResponseWithMappedDataAndPagination() {
      Pageable pageable = PageRequest.of(1, 2, Sort.by("createdAt"));
      OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-29T16:05:27.123-03:00");
      ApiCallHistoryEntity entity = entity(
            "POST",
            "/api/v1/percentage/calculate",
            Map.of("headers", Map.of("content-type", "application/json")),
            200,
            Map.of("result", 9.9),
            createdAt
      );
      when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 5));

      PagedResponse<ApiCallHistoryResponse> response = service.findAll(pageable);

      assertThat(response.data()).hasSize(1);
      ApiCallHistoryResponse item = response.data().getFirst();
      assertThat(item.httpMethod()).isEqualTo("POST");
      assertThat(item.endpoint()).isEqualTo("/api/v1/percentage/calculate");
      assertThat(item.parameters()).isEqualTo(Map.of("headers", Map.of("content-type", "application/json")));
      assertThat(item.httpStatusCode()).isEqualTo(200);
      assertThat(item.response()).isEqualTo(Map.of("result", 9.9));
      assertThat(item.createdAt()).isEqualTo(OffsetDateTime.parse("2026-07-29T16:05:27-03:00"));
      assertThat(response.pagination().page()).isEqualTo(1);
      assertThat(response.pagination().size()).isEqualTo(2);
      assertThat(response.pagination().totalElements()).isEqualTo(5);
      assertThat(response.pagination().totalPages()).isEqualTo(3);
   }

   @Test
   void removesEmptyValuesFromParameters() {
      Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt"));
      Map<String, Object> parameters = new LinkedHashMap<>();
      parameters.put("query", Map.of("num1", "5", "empty", ""));
      parameters.put("path", Map.of());
      parameters.put("headers", new LinkedHashMap<>());
      parameters.put("body", null);
      parameters.put("list", new ArrayList<>(List.of("value", " ", Map.of())));
      ApiCallHistoryEntity entity = entity(
            "GET",
            "/api/v1/call-history",
            parameters,
            200,
            Map.of("status", "UP"),
            OffsetDateTime.parse("2026-07-29T16:05:27-03:00")
      );
      when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

      PagedResponse<ApiCallHistoryResponse> response = service.findAll(pageable);

      assertThat(response.data().getFirst().parameters()).isEqualTo(Map.of(
            "query", Map.of("num1", "5"),
            "list", List.of("value")
      ));
   }

   @Test
   void rejectsNegativePage() {
      Pageable pageable = mock(Pageable.class);
      when(pageable.getPageNumber()).thenReturn(-1);
      when(pageable.getPageSize()).thenReturn(20);
      when(pageable.getSort()).thenReturn(Sort.unsorted());

      assertThatThrownBy(() -> service.findAll(pageable))
            .isInstanceOf(InvalidPageRequestException.class)
            .hasMessage("Page index must not be negative");
   }

   @Test
   void rejectsPageSizeZero() {
      Pageable pageable = mock(Pageable.class);
      when(pageable.getPageNumber()).thenReturn(0);
      when(pageable.getPageSize()).thenReturn(0);
      when(pageable.getSort()).thenReturn(Sort.unsorted());

      assertThatThrownBy(() -> service.findAll(pageable))
            .isInstanceOf(InvalidPageRequestException.class)
            .hasMessage("Page size must be greater than or equal to 1");
   }

   @Test
   void rejectsPageSizeGreaterThanOneHundred() {
      Pageable pageable = PageRequest.of(0, 101);

      assertThatThrownBy(() -> service.findAll(pageable))
            .isInstanceOf(InvalidPageRequestException.class)
            .hasMessage("Page size must be less than or equal to 100");
   }

   @Test
   void rejectsUnsupportedSortField() {
      Pageable pageable = PageRequest.of(0, 20, Sort.by("responseBody"));

      assertThatThrownBy(() -> service.findAll(pageable))
            .isInstanceOf(InvalidSortFieldException.class)
            .hasMessage("Unsupported sorting field: responseBody");
   }

   @Test
   void acceptsAllowedSortFields() {
      for (String sortField : List.of("httpMethod", "endpoint", "httpStatusCode", "createdAt")) {
         Pageable pageable = PageRequest.of(0, 20, Sort.by(sortField));
         when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

         service.findAll(pageable);

         verify(repository).findAll(pageable);
         reset(repository);
      }
   }

   private ApiCallHistoryEntity entity(
         String httpMethod,
         String endpoint,
         Map<String, Object> parameters,
         int httpStatusCode,
         Object responseBody,
         OffsetDateTime createdAt
   ) {
      return ApiCallHistoryEntity.from(new ApiCallHistoryEvent(
            httpMethod,
            endpoint,
            parameters,
            httpStatusCode,
            responseBody,
            createdAt
      ));
   }
}
