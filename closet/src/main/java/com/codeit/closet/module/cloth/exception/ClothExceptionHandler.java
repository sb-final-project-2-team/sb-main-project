package com.codeit.closet.module.cloth.exception;

import com.codeit.closet.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.codeit.closet.module.cloth")
public class ClothExceptionHandler {

    @ExceptionHandler(ClothNotFoundException.class) //
    public ResponseEntity<ErrorResponse> handleClothNotFound(ClothNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateClothNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateClothName(DuplicateClothNameException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidClothAttributeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClothAttribute(InvalidClothAttributeException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ClothAttributeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClothAttributeNotFound(ClothAttributeNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateClothAttributeNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateClothAttributeName(DuplicateClothAttributeNameException e) {
        ErrorResponse error = new ErrorResponse(e, HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
