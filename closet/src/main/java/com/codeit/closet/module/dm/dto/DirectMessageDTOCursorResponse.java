package com.codeit.closet.module.dm.dto;

import java.util.List;
import java.util.UUID;

public record DirectMessageDTOCursorResponse(
        List<DirectMessageDTO> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}