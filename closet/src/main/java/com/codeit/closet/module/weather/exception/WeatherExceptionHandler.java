package com.codeit.closet.module.weather.exception;

import com.codeit.closet.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.codeit.closet.module.weather")
public class WeatherExceptionHandler {

    @ExceptionHandler(WeatherRegionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherRegionNotFound(WeatherRegionNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(WeatherDataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataNotFound(WeatherDataNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(KmaApiException.class)
    public ResponseEntity<ErrorResponse> handleKmaApiException(KmaApiException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_GATEWAY.value());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<ErrorResponse> handleKakaoApiException(KakaoApiException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_GATEWAY.value());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(WeatherDataCollectionException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataCollection(WeatherDataCollectionException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(WeatherException.class)
    public ResponseEntity<ErrorResponse> handleWeatherException(WeatherException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
