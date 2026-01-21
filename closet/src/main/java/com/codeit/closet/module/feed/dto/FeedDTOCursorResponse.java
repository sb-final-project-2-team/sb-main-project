package com.codeit.closet.module.feed.dto;

import java.util.List;
import java.util.UUID;

public record FeedDTOCursorResponse(
        List<FeedDTO> data,

        String nextCursor,
        UUID nextAfter,
        Boolean hasNext,
        Long totalCount,

        String sortBy,
        String sortDirection
) {

  public static FeedDTOCursorResponse empty(String sortBy, String sortDirection) {
    return new FeedDTOCursorResponse(
        List.of(),
        null,
        null,
        false,
        0L,
        sortBy,
        sortDirection
    );
  }
}