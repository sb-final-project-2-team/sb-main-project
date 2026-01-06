package com.codeit.closet.module.cloth.dto;

import java.util.List;
import java.util.UUID;

public record ClothDTOCursorResponse(
        List<ClothDTO> data,

        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,

        String sortBy,
        String sortDirection
) {
}
