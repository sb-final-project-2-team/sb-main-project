package com.codeit.closet.module.recommendation.exception;

/**
 * 사용자가 날씨 지역을 설정하지 않았을 때 발생하는 예외
 */
public class UserWeatherNotSetException extends RuntimeException {

    public UserWeatherNotSetException(String message) {
        super(message);
    }
}
