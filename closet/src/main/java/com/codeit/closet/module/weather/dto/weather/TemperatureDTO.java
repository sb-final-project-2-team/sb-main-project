package com.codeit.closet.module.weather.dto.weather;

public record TemperatureDTO(
        Double current,
        Double comparedToDayBefore,
        Double min,
        Double max
) {
}
