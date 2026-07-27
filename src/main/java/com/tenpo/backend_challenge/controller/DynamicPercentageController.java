package com.tenpo.backend_challenge.controller;

import com.tenpo.backend_challenge.dto.CalculationRequest;
import com.tenpo.backend_challenge.dto.CalculationResponse;
import com.tenpo.backend_challenge.service.DynamicPercentageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/percentage")
@RequiredArgsConstructor
public class DynamicPercentageController {

   private final DynamicPercentageService dynamicPercentageService;

   @PostMapping("/calculate")
   public CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
      return new CalculationResponse(
            dynamicPercentageService.calculate(request.num1(), request.num2())
      );
   }
}
