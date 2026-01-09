package com.codeit.closet.module.cloth.exception;

import java.util.UUID;

public class InvalidClothAttributeException extends RuntimeException {

    public InvalidClothAttributeException(UUID attributeId) {
        super("잘못된 의상 속성입니다: " + attributeId);
    }

    public InvalidClothAttributeException(String message) {
        super(message);
    }
}
