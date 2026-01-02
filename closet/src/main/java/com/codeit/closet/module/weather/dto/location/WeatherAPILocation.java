package com.codeit.closet.module.weather.dto.location;

import java.util.List;

public record WeatherAPILocation(
        Double latitude,
        Double longitude,
        Integer x,
        Integer y,
        List<String> locationNames
) {
}
