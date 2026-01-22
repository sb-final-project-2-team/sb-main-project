package com.codeit.closet.module.weather.exception;

/**
 * 날씨 도메인 기본 예외 클래스
 */
public class WeatherException extends RuntimeException {

    public WeatherException(String message) {
        super(message);
    }

    public WeatherException(String message, Throwable cause) {
        super(message, cause);
    }
}
