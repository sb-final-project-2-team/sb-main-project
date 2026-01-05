package com.codeit.closet.module.comment.dto;

import com.codeit.closet.module.user.dto.user.UserDTO;
import java.util.List;
import java.util.UUID;

public record CommentDTOCursorResponse(
        List<CommentDTO> data,

        String nextCursor,
        UUID nextAfter,
        Boolean hasNext,
        Long totalCount,

        String sortBy,
        String sortDirection
) {
}