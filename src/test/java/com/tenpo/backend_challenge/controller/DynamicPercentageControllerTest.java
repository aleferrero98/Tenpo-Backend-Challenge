package com.tenpo.backend_challenge.controller;

import com.tenpo.backend_challenge.exception.handler.GlobalExceptionHandler;
import com.tenpo.backend_challenge.exception.PercentageProviderUnavailableException;
import com.tenpo.backend_challenge.service.DynamicPercentageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DynamicPercentageControllerTest {

   private final DynamicPercentageService dynamicPercentageService = mock(DynamicPercentageService.class);

   private MockMvc mockMvc;

   @BeforeEach
   void setUp() {
      LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
      validator.afterPropertiesSet();

      mockMvc = MockMvcBuilders
            .standaloneSetup(new DynamicPercentageController(dynamicPercentageService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
   }

   @Test
   void calculateReturnsResultOk() throws Exception {
      when(dynamicPercentageService.calculate(eq(BigDecimal.valueOf(5)), eq(BigDecimal.valueOf(5))))
            .thenReturn(BigDecimal.valueOf(11));

      mockMvc.perform(post("/api/v1/percentage/calculate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"num1\": 5, \"num2\": 5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(11));
   }

   @Test
   void calculateReturnsBadRequestWhenNum1IsMissing() throws Exception {
      mockMvc.perform(post("/api/v1/percentage/calculate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"num2\": 5}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.num1").value("num1 is required"));
   }

   @Test
   void calculateReturnsBadRequestWhenNum2IsMissing() throws Exception {
      mockMvc.perform(post("/api/v1/percentage/calculate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"num1\": 5}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.num2").value("num2 is required"));
   }

   @Test
   void calculateReturnsServiceUnavailableWhenProviderFails() throws Exception {
      when(dynamicPercentageService.calculate(eq(BigDecimal.valueOf(5)), eq(BigDecimal.valueOf(5))))
            .thenThrow(new PercentageProviderUnavailableException(
                  "Percentage provider is unavailable after 3 attempts",
                  new RuntimeException("Provider failure")
            ));

      mockMvc.perform(post("/api/v1/percentage/calculate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"num1\": 5, \"num2\": 5}"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.message").value("Percentage provider is unavailable after 3 attempts"));
   }
}
