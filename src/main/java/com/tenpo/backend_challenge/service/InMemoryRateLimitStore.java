package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.dto.RateLimitResult;
import com.tenpo.backend_challenge.dto.RateLimitState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimitStore {

   private final ConcurrentHashMap<String, RateLimitState> clients;
   private final int maxRequests;
   private final Duration windowDuration;

   public InMemoryRateLimitStore(
         @Value("${rate-limit.max-requests}") int maxRequests,
         @Value("${rate-limit.window-seconds}") long windowSeconds
   ) {
      this.maxRequests = maxRequests;
      this.windowDuration = Duration.ofSeconds(windowSeconds);
      this.clients = new ConcurrentHashMap<>();
   }

   public RateLimitResult registerRequest(String clientIp, Instant now) {
      RateLimitState updatedState = clients.compute(clientIp, (ip, currentState) -> {
         if (currentState == null || isExpired(currentState, now)) {
            return new RateLimitState(1, now);
         }

         int nextCount = Math.min(
               currentState.requestCount() + 1,
               maxRequests + 1
         );

         return new RateLimitState(nextCount, currentState.windowStart());
      });

      boolean allowed = updatedState.requestCount() <= maxRequests;
      long retryAfterSeconds = allowed ? 0 : retryAfterSeconds(updatedState, now);

      return new RateLimitResult(allowed, retryAfterSeconds);
   }

   public void removeExpiredEntries() {
      Instant now = Instant.now();
      clients.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
   }

   private boolean isExpired(RateLimitState state, Instant now) {
      return !state.windowStart().plus(windowDuration).isAfter(now);
   }

   private long retryAfterSeconds(RateLimitState state, Instant now) {
      Instant windowEnd = state.windowStart().plus(windowDuration);
      long remainingSeconds = Duration.between(now, windowEnd).toSeconds();

      return Math.max(1, remainingSeconds);
   }
}
