package com.codeit.closet.module.weather.dto.weather;

import com.codeit.closet.module.weather.dto.location.LocationDTO;
import com.codeit.closet.module.weather.entity.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record WeatherDTO(
        UUID id,
        Instant forecastedAt,
        Instant forecastAt,
        LocationDTO location,
        SkyStatus skyStatus,
        PrecipitationDTO precipitation,
        HumidityDTO humidity,
        TemperatureDTO temperature,
        WindSpeedDTO windSpeed
) {
}
