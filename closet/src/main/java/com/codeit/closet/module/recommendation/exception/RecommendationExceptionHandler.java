package com.codeit.closet.module.recommendation.exception;

import com.codeit.closet.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 추천 모듈 예외 핸들러
 */
@RestControllerAdvice(basePackages = "com.codeit.closet.module.recommendation")
public class RecommendationExceptionHandler {

    @ExceptionHandler(WeatherNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherNotFound(WeatherNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserWeatherNotSetException.class)
    public ResponseEntity<ErrorResponse> handleUserWeatherNotSet(UserWeatherNotSetException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InsufficientClothesException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientClothes(InsufficientClothesException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
