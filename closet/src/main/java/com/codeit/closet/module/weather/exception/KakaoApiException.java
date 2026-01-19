package com.codeit.closet.module.weather.exception;

/**
 * 카카오 API 호출 관련 예외
 */
public class KakaoApiException extends WeatherApiException {

    public KakaoApiException(String message) {
        super("카카오 API 오류: " + message);
    }

    public KakaoApiException(String message, Throwable cause) {
        super("카카오 API 오류: " + message, cause);
    }

    public static KakaoApiException regionNotFound(Double longitude, Double latitude) {
        return new KakaoApiException(
                "좌표(" + longitude + ", " + latitude + ")에 대한 지역 정보가 존재하지 않습니다");
    }
}
