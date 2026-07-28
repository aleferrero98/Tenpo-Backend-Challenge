package com.tenpo.backend_challenge.exception;

import com.tenpo.backend_challenge.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException exception) {
      Map<String, String> errors = new LinkedHashMap<>();

      exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
      );

      return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("Validation failed", errors));
   }

   @ExceptionHandler(PercentageProviderUnavailableException.class)
   public ResponseEntity<ErrorResponse> handlePercentageProviderUnavailable(
         PercentageProviderUnavailableException exception
   ) {
      return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse(exception.getMessage()));
   }

   @ExceptionHandler({
         InvalidPageRequestException.class,
         InvalidSortFieldException.class,
         MethodArgumentTypeMismatchException.class,
         IllegalArgumentException.class
   })
   public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
      return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(exception.getMessage()));
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception exception) {
      return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred"));
   }
}
