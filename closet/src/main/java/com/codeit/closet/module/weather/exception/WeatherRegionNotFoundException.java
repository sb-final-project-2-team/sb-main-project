package com.codeit.closet.module.weather.exception;

import java.util.UUID;

/**
 * WeatherRegion이 등록되지 않았을 때 발생하는 예외
 */
public class WeatherRegionNotFoundException extends WeatherException {

    public WeatherRegionNotFoundException(Integer nx, Integer ny) {
        super("해당 격자 좌표의 지역이 등록되지 않았습니다: nx=" + nx + ", ny=" + ny);
    }

    public WeatherRegionNotFoundException(UUID weatherRegionId) {
        super("WeatherRegion을 찾을 수 없습니다: " + weatherRegionId);
    }
}
