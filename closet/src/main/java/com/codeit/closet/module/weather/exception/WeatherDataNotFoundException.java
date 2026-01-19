package com.codeit.closet.module.weather.exception;

import java.util.UUID;

/**
 * 날씨 데이터를 찾을 수 없을 때 발생하는 예외
 */
public class WeatherDataNotFoundException extends WeatherException {

    public WeatherDataNotFoundException(UUID weatherRegionId) {
        super("날씨 데이터를 찾을 수 없습니다: weatherRegionId=" + weatherRegionId);
    }

    public WeatherDataNotFoundException(String message) {
        super(message);
    }
}
