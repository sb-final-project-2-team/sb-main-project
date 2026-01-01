package com.codeit.closet.module.user.dto.user;

import com.codeit.closet.module.user.entity.UserRole;

public record UserRoleUpdateRequest(
        UserRole role
) {
}
