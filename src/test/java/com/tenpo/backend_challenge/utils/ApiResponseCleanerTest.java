package com.tenpo.backend_challenge.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseCleanerTest {

   @Test
   void removeEmptyValueReturnsNullForNullBlankEmptyMapAndEmptyList() {
      assertThat(ApiResponseCleaner.removeEmptyValue(null)).isNull();
      assertThat(ApiResponseCleaner.removeEmptyValue(" ")).isNull();
      assertThat(ApiResponseCleaner.removeEmptyValue(Map.of())).isNull();
      assertThat(ApiResponseCleaner.removeEmptyValue(List.of())).isNull();
   }

   @Test
   void removeEmptyValuesCleansNestedMaps() {
      Map<String, Object> value = Map.of(
            "query", Map.of("num1", "5", "empty", ""),
            "path", Map.of(),
            "body", Map.of("nested", Map.of("value", 10, "blank", " "))
      );

      Map<String, Object> result = ApiResponseCleaner.removeEmptyValues(value);

      assertThat(result).containsOnlyKeys("query", "body");
      assertThat(result.get("query")).isEqualTo(Map.of("num1", "5"));
      assertThat(result.get("body")).isEqualTo(Map.of("nested", Map.of("value", 10)));
   }

   @Test
   void removeEmptyValuesCleansNestedLists() {
      Map<String, Object> value = Map.of(
            "items", List.of("value", " ", Map.of(), Map.of("key", "kept")),
            "emptyItems", List.of(" ", Map.of())
      );

      Map<String, Object> result = ApiResponseCleaner.removeEmptyValues(value);

      assertThat(result).containsOnlyKeys("items");
      assertThat(result.get("items")).isEqualTo(List.of("value", Map.of("key", "kept")));
   }
}
