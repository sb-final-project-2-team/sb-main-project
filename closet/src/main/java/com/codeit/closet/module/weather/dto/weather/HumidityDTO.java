package com.codeit.closet.module.weather.dto.weather;

public record HumidityDTO(
        Double current,
        Double comparedToDayBefore
) {
}
