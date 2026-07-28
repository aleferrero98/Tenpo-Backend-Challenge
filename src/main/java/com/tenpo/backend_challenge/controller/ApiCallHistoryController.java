package com.tenpo.backend_challenge.controller;

import com.tenpo.backend_challenge.dto.response.ApiCallHistoryResponse;
import com.tenpo.backend_challenge.dto.response.PagedResponse;
import com.tenpo.backend_challenge.service.ApiCallHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/call-history")
@RequiredArgsConstructor
public class ApiCallHistoryController {

   private final ApiCallHistoryService apiCallHistoryService;

   @GetMapping
   public ResponseEntity<PagedResponse<ApiCallHistoryResponse>> getHistory(@PageableDefault(
         page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
      return ResponseEntity.ok(apiCallHistoryService.findAll(pageable));
   }
}
