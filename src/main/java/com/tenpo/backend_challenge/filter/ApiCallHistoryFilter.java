package com.tenpo.backend_challenge.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.backend_challenge.dto.ApiCallHistoryEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Order(1)
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiCallHistoryFilter extends OncePerRequestFilter {

   private static final int MAX_BODY_SIZE = 10_000;

   private static final Set<String> SENSITIVE_HEADERS = Set.of(
         "authorization",
         "proxy-authorization",
         "cookie",
         "set-cookie",
         "x-api-key",
         "api-key",
         "x-auth-token",
         "x-access-token"
   );

   private final ApplicationEventPublisher eventPublisher;
   private final ObjectMapper objectMapper;

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {
      ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_BODY_SIZE + 1);
      ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

      try {
         filterChain.doFilter(requestWrapper, responseWrapper);
      } finally {
         try {
            eventPublisher.publishEvent(buildEvent(requestWrapper, responseWrapper));
         } catch (Exception exception) {
            log.error("Could not publish API call history event", exception);
         }

         responseWrapper.copyBodyToResponse();
      }
   }

   private ApiCallHistoryEvent buildEvent(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
      return new ApiCallHistoryEvent(
            request.getMethod(),
            getEndpoint(request),
            buildParameters(request),
            response.getStatus(),
            parseBody(response.getContentAsByteArray(), getCharset(response.getCharacterEncoding())),
            OffsetDateTime.now()
      );
   }

   private String getEndpoint(HttpServletRequest request) {
      Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

      if (pattern instanceof String endpoint) {
         return endpoint;
      }

      return request.getRequestURI();
   }

   private Map<String, Object> buildParameters(ContentCachingRequestWrapper request) {
      Map<String, Object> parameters = new LinkedHashMap<>();

      Map<String, Object> query = getQueryParameters(request);
      if (!query.isEmpty()) {
         parameters.put("query", query);
      }

      Map<String, Object> path = getPathVariables(request);
      if (!path.isEmpty()) {
         parameters.put("path", path);
      }

      Map<String, Object> headers = getHeaders(request);
      if (!headers.isEmpty()) {
         parameters.put("headers", headers);
      }

      Object body = parseBody(request.getContentAsByteArray(), getCharset(request.getCharacterEncoding()));
      if (body != null) {
         parameters.put("body", body);
      }

      return parameters.isEmpty() ? null : parameters;
   }

   private Map<String, Object> getQueryParameters(HttpServletRequest request) {
      Map<String, Object> query = new LinkedHashMap<>();

      request.getParameterMap().forEach((name, values) -> query.put(name, toJsonValue(values)));

      return query;
   }

   private Map<String, Object> getPathVariables(HttpServletRequest request) {
      Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      Map<String, Object> path = new LinkedHashMap<>();

      if (pathVariables instanceof Map<?, ?> variables) {
         variables.forEach((name, value) -> path.put(String.valueOf(name), value));
      }

      return path;
   }

   private Map<String, Object> getHeaders(HttpServletRequest request) {
      Map<String, Object> headers = new LinkedHashMap<>();
      Enumeration<String> headerNames = request.getHeaderNames();

      if (headerNames == null) {
         return headers;
      }

      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         String normalizedHeaderName = headerName.toLowerCase(Locale.ROOT);

         if (!SENSITIVE_HEADERS.contains(normalizedHeaderName)) {
            headers.put(normalizedHeaderName, toJsonValue(Collections.list(request.getHeaders(headerName))));
         }
      }

      return headers;
   }

   private Object parseBody(byte[] content, Charset charset) {
      if (content.length == 0) {
         return null;
      }

      String body = new String(content, charset);

      if (body.length() > MAX_BODY_SIZE) {
         return Map.of(
               "raw", body.substring(0, MAX_BODY_SIZE) + "...",
               "truncated", true
         );
      }

      try {
         return objectMapper.readValue(body, Object.class);
      } catch (JsonProcessingException exception) {
         return Map.of("raw", body);
      }
   }

   private Object toJsonValue(String[] values) {
      if (values == null || values.length == 0) {
         return null;
      }

      if (values.length == 1) {
         return values[0];
      }

      return List.of(values);
   }

   private Object toJsonValue(List<String> values) {
      if (values.isEmpty()) {
         return null;
      }

      if (values.size() == 1) {
         return values.getFirst();
      }

      return new ArrayList<>(values);
   }

   private Charset getCharset(String characterEncoding) {
      if (characterEncoding == null) {
         return StandardCharsets.UTF_8;
      }

      try {
         return Charset.forName(characterEncoding);
      } catch (Exception exception) {
         return StandardCharsets.UTF_8;
      }
   }
}
