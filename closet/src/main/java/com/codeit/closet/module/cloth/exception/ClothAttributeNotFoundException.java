package com.codeit.closet.module.cloth.exception;

import java.util.UUID;

public class ClothAttributeNotFoundException extends RuntimeException {

    public ClothAttributeNotFoundException(UUID attributeId) {
        super("의상 속성 정의를 찾을 수 없습니다: " + attributeId);
    }
}
