package com.tenpo.backend_challenge.service;

import com.tenpo.backend_challenge.dto.RateLimitResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimitStoreTest {

   private static final Instant WINDOW_START = Instant.parse("2026-07-29T12:00:00Z");

   private final InMemoryRateLimitStore store = new InMemoryRateLimitStore(3, 60);

   @Test
   void allowsRequestsUpToConfiguredLimit() {
      assertThat(store.registerRequest("192.168.1.10", WINDOW_START).allowed()).isTrue();
      assertThat(store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(10)).allowed()).isTrue();
      assertThat(store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(20)).allowed()).isTrue();
   }

   @Test
   void rejectsFourthRequestWithinSameWindow() {
      store.registerRequest("192.168.1.10", WINDOW_START);
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(10));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(20));

      RateLimitResult result = store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(30));

      assertThat(result.allowed()).isFalse();
      assertThat(result.retryAfterSeconds()).isEqualTo(30);
   }

   @Test
   void createsIndependentCountersPerIp() {
      store.registerRequest("192.168.1.10", WINDOW_START);
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(10));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(20));
      RateLimitResult blockedIpResult = store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(30));

      RateLimitResult otherIpResult = store.registerRequest("192.168.1.20", WINDOW_START.plusSeconds(30));

      assertThat(blockedIpResult.allowed()).isFalse();
      assertThat(otherIpResult.allowed()).isTrue();
   }

   @Test
   void startsNewWindowAfterWindowExpires() {
      store.registerRequest("192.168.1.10", WINDOW_START);
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(10));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(20));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(30));

      RateLimitResult result = store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(60));

      assertThat(result.allowed()).isTrue();
      assertThat(result.retryAfterSeconds()).isZero();
   }

   @Test
   void removeExpiredEntriesAllowsNewWindow() {
      store.registerRequest("192.168.1.10", WINDOW_START);
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(10));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(20));
      store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(30));

      store.removeExpiredEntries();

      RateLimitResult result = store.registerRequest("192.168.1.10", WINDOW_START.plusSeconds(61));

      assertThat(result.allowed()).isTrue();
   }
}
