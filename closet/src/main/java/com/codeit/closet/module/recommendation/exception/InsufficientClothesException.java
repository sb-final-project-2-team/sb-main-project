package com.codeit.closet.module.recommendation.exception;

/**
 * 추천에 필요한 의상이 부족할 때 발생하는 예외
 */
public class InsufficientClothesException extends RuntimeException {

    public InsufficientClothesException(String message) {
        super(message);
    }
}
