package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.dto.ApiCallHistoryEvent;
import com.tenpo.backend_challenge.model.ApiCallHistoryEntity;
import com.tenpo.backend_challenge.repository.ApiCallHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallHistoryListener {

   private final ApiCallHistoryRepository repository;

   @Async("apiCallHistoryExecutor")
   @EventListener
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void handle(ApiCallHistoryEvent event) {
      try {
         repository.save(ApiCallHistoryEntity.from(event));
      } catch (Exception exception) {
         log.error("Could not persist API call history", exception);
      }
   }
}
