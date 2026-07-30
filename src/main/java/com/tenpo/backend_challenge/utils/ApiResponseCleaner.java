package com.tenpo.backend_challenge.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiResponseCleaner {

   public static Map<String, Object> removeEmptyValues(Map<String, Object> values) {
      return cleanMap(values);
   }

   public static Object removeEmptyValue(Object value) {
      return switch (value) {
         case null -> null;

         case String text when text.isBlank() -> null;

         case Map<?, ?> map -> cleanMap(map);

         case List<?> list -> cleanList(list);

         default -> value;
      };
   }

   private static Map<String, Object> cleanMap(Map<?, ?> map) {
      Map<String, Object> cleaned = new LinkedHashMap<>();

      map.forEach((key, value) -> {
         Object cleanedValue = removeEmptyValue(value);

         if (cleanedValue != null) {
            cleaned.put(String.valueOf(key), cleanedValue);
         }
      });

      return cleaned.isEmpty() ? null : cleaned;
   }

   private static List<Object> cleanList(List<?> list) {
      List<Object> cleaned = new ArrayList<>();

      for (Object item : list) {
         Object cleanedValue = removeEmptyValue(item);

         if (cleanedValue != null) {
            cleaned.add(cleanedValue);
         }
      }

      return cleaned.isEmpty() ? null : cleaned;
   }
}
