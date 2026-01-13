package com.codeit.closet.module.weather.dto.weather;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.entity.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record WeatherDTO(
        UUID id,
        Instant forecastedAt,
        Instant forecastAt,
        WeatherAPILocation location,
        SkyStatus skyStatus,
        PrecipitationDTO precipitation,
        HumidityDTO humidity,
        TemperatureDTO temperature,
        WindSpeedDTO windSpeed
) {
}
