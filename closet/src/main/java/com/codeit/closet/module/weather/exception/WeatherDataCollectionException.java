package com.codeit.closet.module.weather.exception;

/**
 * 날씨 데이터 수집 실패 시 발생하는 예외
 */
public class WeatherDataCollectionException extends WeatherException {

    public WeatherDataCollectionException(String message, Throwable cause) {
        super("날씨 데이터 수집에 실패했습니다: " + message, cause);
    }
}
