package com.codeit.closet.module.weather.exception;

/**
 * 기상청 API 호출 관련 예외
 */
public class KmaApiException extends WeatherApiException {

    public KmaApiException(String message) {
        super("기상청 API 오류: " + message);
    }

    public static KmaApiException invalidResponse() {
        return new KmaApiException("잘못된 API 응답 형식입니다");
    }

    public static KmaApiException emptyData() {
        return new KmaApiException("API 응답에 데이터가 없습니다");
    }
}
