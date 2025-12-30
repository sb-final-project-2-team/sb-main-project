package com.codeit.closet.module.user.dto.user;

public record UserCreateRequest(
        String name,
        String password,
        String email
) {
}
