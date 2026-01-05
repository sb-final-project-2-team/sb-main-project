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
}