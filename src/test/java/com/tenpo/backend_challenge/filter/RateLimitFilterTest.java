package com.tenpo.backend_challenge.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tenpo.backend_challenge.dto.RateLimitResult;
import com.tenpo.backend_challenge.service.InMemoryRateLimitStore;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

   private final InMemoryRateLimitStore rateLimitStore = mock(InMemoryRateLimitStore.class);
   private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
   private final RateLimitFilter filter = new RateLimitFilter(rateLimitStore, objectMapper);

   @Test
   void continuesFilterChainWhenRequestIsAllowed() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/call-history");
      request.setRemoteAddr("192.168.1.10");
      MockHttpServletResponse response = new MockHttpServletResponse();
      FilterChain filterChain = mock(FilterChain.class);
      when(rateLimitStore.registerRequest(eq("192.168.1.10"), any())).thenReturn(new RateLimitResult(true, 0));

      filter.doFilter(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
   }

   @Test
   void returnsTooManyRequestsWhenLimitExceeded() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/call-history");
      request.setRemoteAddr("192.168.1.10");
      MockHttpServletResponse response = new MockHttpServletResponse();
      FilterChain filterChain = mock(FilterChain.class);
      when(rateLimitStore.registerRequest(eq("192.168.1.10"), any())).thenReturn(new RateLimitResult(false, 27));

      filter.doFilter(request, response, filterChain);

      assertThat(response.getStatus()).isEqualTo(429);
      assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("27");
      assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
      assertThat(response.getContentAsString()).contains("Rate limit exceeded. Maximum 3 requests per minute.");
      verify(filterChain, never()).doFilter(any(), any());
   }

   @Test
   void doesNotFilterUrlsContainingSwagger() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/docs/swagger-custom-path");
      MockHttpServletResponse response = new MockHttpServletResponse();
      FilterChain filterChain = mock(FilterChain.class);

      filter.doFilter(request, response, filterChain);

      verify(rateLimitStore, never()).registerRequest(any(), any());
      verify(filterChain).doFilter(request, response);
   }
}
