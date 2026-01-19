package com.codeit.closet.module.weather.exception;

/**
 * 날씨 데이터를 찾을 수 없을 때 발생하는 예외
 * 향후 사용을 위해 예외 클래스만 정의 (현재는 사용되지 않음)
 */
public class WeatherDataNotFoundException extends WeatherException {

    public WeatherDataNotFoundException() {
        super("날씨 데이터를 찾을 수 없습니다");
    }
}
