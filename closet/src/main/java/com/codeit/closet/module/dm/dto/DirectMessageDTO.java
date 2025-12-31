package com.codeit.closet.module.dm.dto;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageDTO(
        UUID id,
        Instant createdAt,
//        UserSummaryDto sender,
//        UserSummaryDto receiver,
        String content
) {
}