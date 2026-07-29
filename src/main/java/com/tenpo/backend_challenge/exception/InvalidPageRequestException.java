package com.tenpo.backend_challenge.exception;

public class InvalidPageRequestException extends RuntimeException {

   public InvalidPageRequestException(String message) {
      super(message);
   }
}
