package com.codeit.closet.module.user.dto.profile;

import com.codeit.closet.common.util.converter.FlexibleInstantDeserializer;
import com.codeit.closet.module.user.entity.UserGender;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

public record ProfileUpdateRequest(
        String name,
        UserGender gender,

        @JsonDeserialize(using = FlexibleInstantDeserializer.class)
        Instant birthDate,
        //LocationDto location,              // 위치 정보 객체
        Integer temperatureSensitivity
) {
}
