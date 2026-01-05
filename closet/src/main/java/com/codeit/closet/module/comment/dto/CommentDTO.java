package com.codeit.closet.module.comment.dto;

import com.codeit.closet.module.user.dto.user.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record CommentDTO(
    UUID id,
    Instant createdAt,
    UUID feedId,
    UserSummary author,
    String content
) {
}
