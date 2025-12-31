package com.codeit.closet.module.dm.dto;

import com.codeit.closet.module.user.dto.user.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageDTO(
        UUID id,
        Instant createdAt,
        UserSummary sender,
        UserSummary receiver,
        String content
) {
}