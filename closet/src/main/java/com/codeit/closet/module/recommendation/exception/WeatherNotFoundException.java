package com.codeit.closet.module.recommendation.exception;

import java.util.UUID;

/**
 * 날씨 데이터를 찾을 수 없을 때 발생하는 예외
 */
public class WeatherNotFoundException extends RuntimeException {

    public WeatherNotFoundException(UUID weatherId) {
        super("날씨 데이터를 찾을 수 없습니다: " + weatherId);
    }
}
