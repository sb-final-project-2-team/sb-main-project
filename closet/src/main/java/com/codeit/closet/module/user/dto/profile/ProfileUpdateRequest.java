package com.codeit.closet.module.user.dto.profile;

import com.codeit.closet.module.user.entity.UserGender;

public record ProfileUpdateRequest(
        String name,
        UserGender gender,
        String birthDate,
        //LocationDto location,              // 위치 정보 객체
        Integer temperatureSensitivity
) {
}
