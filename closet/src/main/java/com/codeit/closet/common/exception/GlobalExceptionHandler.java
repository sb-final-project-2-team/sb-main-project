package com.codeit.closet.common.exception;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("예외 발생 : code={}, message={}", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e, 500);
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.error("예외 발생 : code={}, message={}", HttpStatus.BAD_REQUEST, e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e, 400);
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
    log.error("예외 발생 : code={}, message={}", HttpStatus.NOT_FOUND, e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e, 404);
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(FileNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException e) {
    log.error("예외 발생 : code={}, message={}", HttpStatus.NOT_FOUND, e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e, 404);
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(ClosetException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(ClosetException e) {
    log.error("커스텀 예외 발생 : code={}, message={}", e.getErrorCode(), e.getMessage());
    HttpStatus httpStatus = determineHttpStatus(e);
    ErrorResponse errorResponse = new ErrorResponse(e, httpStatus.value());
    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
  }

  private HttpStatus determineHttpStatus(ClosetException exception) {
    return switch (exception.getErrorCode()) {
      // 404 Not Found
      case USER_NOT_FOUND,
           BINARY_CONTENT_NOT_FOUND -> HttpStatus.NOT_FOUND;

      // 409 Conflict
      case DUPLICATE_USER -> HttpStatus.CONFLICT;

      // 401 Unauthorized
      case INVALID_USER_CREDENTIALS -> HttpStatus.UNAUTHORIZED;

      // 400 Bad Request
      case INVALID_REQUEST,
           INVALID_USER_PARAMETER -> HttpStatus.BAD_REQUEST;

      // 500 Internal Server Error
      case INTERNAL_SERVER_ERROR,
           BINARY_CONTENT_SAVE_FAILED,
           BINARY_CONTENT_DELETE_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    log.error("요청 유효성 검사 실패: {}", ex.getMessage());

    Map<String, String> validationErrors = new HashMap<>();

    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    ErrorResponse errorResponse = new ErrorResponse(
        "VALIDATION_ERROR",
        "요청 데이터 유효성 검사에 실패했습니다",
        validationErrors,
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity
        .status(errorResponse.getStatus())
        .body(errorResponse);
  }
}
