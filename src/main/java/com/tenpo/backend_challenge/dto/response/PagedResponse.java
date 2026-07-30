package com.tenpo.backend_challenge.dto.response;

import java.util.List;

public record PagedResponse<T>(
      List<T> data,
      Pagination pagination
) {
}
