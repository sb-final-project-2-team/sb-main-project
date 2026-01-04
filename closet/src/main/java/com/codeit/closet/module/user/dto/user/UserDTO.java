package com.codeit.closet.module.user.dto.user;

import com.codeit.closet.module.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserDTO(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        UserRole role,
        Boolean locked
) {
}