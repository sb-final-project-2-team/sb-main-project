package com.codeit.closet.module.cloth.exception;

public class DuplicateClothAttributeNameException extends RuntimeException {

    public DuplicateClothAttributeNameException(String name) {
        super("이미 존재하는 속성 이름입니다: " + name);
    }
}
