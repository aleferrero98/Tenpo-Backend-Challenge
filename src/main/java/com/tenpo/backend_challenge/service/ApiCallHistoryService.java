package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.dto.ApiCallHistoryResponse;
import com.tenpo.backend_challenge.dto.PagedResponse;
import com.tenpo.backend_challenge.dto.Pagination;
import com.tenpo.backend_challenge.exception.InvalidPageRequestException;
import com.tenpo.backend_challenge.exception.InvalidSortFieldException;
import com.tenpo.backend_challenge.model.ApiCallHistoryEntity;
import com.tenpo.backend_challenge.repository.ApiCallHistoryRepository;
import com.tenpo.backend_challenge.utils.ApiResponseCleaner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiCallHistoryService {

   private static final int MAX_PAGE_SIZE = 100;

   private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
         "httpMethod",
         "endpoint",
         "httpStatusCode",
         "createdAt"
   );

   private final ApiCallHistoryRepository repository;

   public PagedResponse<ApiCallHistoryResponse> findAll(Pageable pageable) {
      validatePageable(pageable);
      validateSorting(pageable.getSort());

      Page<ApiCallHistoryEntity> result = repository.findAll(pageable);
      List<ApiCallHistoryResponse> data = result.getContent()
            .stream()
            .map(this::toResponse)
            .toList();

      return new PagedResponse<>(
            data,
            new Pagination(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages())
      );
   }

   private void validatePageable(Pageable pageable) {
      if (pageable.getPageNumber() < 0) {
         throw new InvalidPageRequestException("Page index must not be negative");
      }

      if (pageable.getPageSize() < 1) {
         throw new InvalidPageRequestException("Page size must be greater than or equal to 1");
      }

      if (pageable.getPageSize() > MAX_PAGE_SIZE) {
         throw new InvalidPageRequestException("Page size must be less than or equal to " + MAX_PAGE_SIZE);
      }
   }

   private void validateSorting(Sort sort) {
      for (Sort.Order order : sort) {
         if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
            throw new InvalidSortFieldException(order.getProperty());
         }
      }
   }

   private ApiCallHistoryResponse toResponse(ApiCallHistoryEntity entity) {
      return new ApiCallHistoryResponse(
            entity.getHttpMethod(),
            entity.getEndpoint(),
            ApiResponseCleaner.removeEmptyValues(entity.getParameters()),
            entity.getHttpStatusCode(),
            ApiResponseCleaner.removeEmptyValue(entity.getResponseBody()),
            entity.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
      );
   }
}
