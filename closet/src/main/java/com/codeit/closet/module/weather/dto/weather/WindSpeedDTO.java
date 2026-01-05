package com.codeit.closet.module.weather.dto.weather;

import com.codeit.closet.module.weather.entity.WindStrength;

public record WindSpeedDTO(
        Double speed,
        WindStrength asWord
) {
}
