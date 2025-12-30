package com.codeit.closet.module.user.dto.user;

import java.util.List;
import java.util.UUID;

public record UserDTOCursorResponse(
        List<UserDTO> data,

        String nextCursor,
        UUID nextAfter,
        boolean hasNext,
        long totalCount,

        String sortBy,
        String sortDirection
) {
}