package com.codeit.closet.module.weather.dto.weather;

import com.codeit.closet.module.weather.entity.PrecipitationType;

public record PrecipitationDTO(
        PrecipitationType type,
        Double amount,
        Double probability
) {
}
