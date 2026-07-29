package com.tenpo.backend_challenge.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.backend_challenge.dto.response.ErrorResponse;
import com.tenpo.backend_challenge.dto.RateLimitResult;
import com.tenpo.backend_challenge.service.InMemoryRateLimitStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Order(2)
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

   private static final String ERROR_MESSAGE = "Rate limit exceeded. Maximum 3 requests per minute.";

   private final InMemoryRateLimitStore rateLimitStore;
   private final ObjectMapper objectMapper;

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {
      RateLimitResult result = rateLimitStore.registerRequest(resolveClientIp(request), Instant.now());

      if (result.allowed()) {
         filterChain.doFilter(request, response);
         return;
      }

      writeTooManyRequestsResponse(response, result.retryAfterSeconds());
   }

   private String resolveClientIp(HttpServletRequest request) {
      return request.getRemoteAddr();
   }

   private void writeTooManyRequestsResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

      objectMapper.writeValue(
            response.getWriter(),
            new ErrorResponse(ERROR_MESSAGE)
      );
   }
}
