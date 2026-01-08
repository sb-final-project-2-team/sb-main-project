package com.codeit.closet.module.user.dto.user;

import java.util.List;
import java.util.UUID;

public record UserDTOCursorResponse(
        List<UserDTO> data,

        String nextCursor,
        UUID nextAfter,
        Boolean hasNext,
        Long totalCount,

        String sortBy,
        String sortDirection
) {
}