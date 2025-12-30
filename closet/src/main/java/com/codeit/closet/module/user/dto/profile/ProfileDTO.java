package com.codeit.closet.module.user.dto.profile;

import com.codeit.closet.module.user.entity.UserGender;

import java.time.Instant;

public record ProfileDTO(
        String userId,
        String name,
        UserGender gender,
        Instant birthDate,
        // LocationDto location,
        Integer temperatureSensitivity,
        String profileImageUrl
) {
}
