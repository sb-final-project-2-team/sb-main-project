package com.codeit.closet.module.weather.exception;

/**
 * 외부 API 통신 관련 기본 예외 클래스
 */
public class WeatherApiException extends WeatherException {

    public WeatherApiException(String message) {
        super(message);
    }
}
