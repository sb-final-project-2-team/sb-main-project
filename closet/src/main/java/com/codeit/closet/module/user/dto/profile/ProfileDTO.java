package com.codeit.closet.module.user.dto.profile;

import com.codeit.closet.common.util.converter.InstantToDateSerializer;
import com.codeit.closet.module.user.entity.UserGender;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.UUID;

public record ProfileDTO(
        UUID userId,
        String name,
        UserGender gender,

        @JsonSerialize(using = InstantToDateSerializer.class)
        Instant birthDate,

        // LocationDto location,
        Integer temperatureSensitivity,
        String profileImageUrl
) {
}
