package com.codeit.closet.module.weather.dto.weather;

import com.codeit.closet.module.weather.entity.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDTO(
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationDTO precipitation,
    TemperatureDTO temperature
){

}
