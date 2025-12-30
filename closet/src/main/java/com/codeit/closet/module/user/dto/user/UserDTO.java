package com.codeit.closet.module.user.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserDTO(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        String role,
        boolean locked
) {
}