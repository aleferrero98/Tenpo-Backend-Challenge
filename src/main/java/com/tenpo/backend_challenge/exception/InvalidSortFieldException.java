package com.tenpo.backend_challenge.exception;

public class InvalidSortFieldException extends RuntimeException {

   public InvalidSortFieldException(String field) {
      super("Unsupported sorting field: " + field);
   }
}
