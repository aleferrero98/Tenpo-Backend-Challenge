package com.tenpo.backend_challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitCleanupScheduler {

   private static final long CLEANUP_INTERVAL_MILLISECONDS = 300_000;

   private final InMemoryRateLimitStore rateLimitStore;

   @Scheduled(fixedRate = CLEANUP_INTERVAL_MILLISECONDS)
   public void removeExpiredEntries() {
      rateLimitStore.removeExpiredEntries();
   }
}
