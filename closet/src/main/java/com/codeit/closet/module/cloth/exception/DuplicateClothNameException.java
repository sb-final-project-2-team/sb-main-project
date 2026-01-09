package com.codeit.closet.module.cloth.exception;

public class DuplicateClothNameException extends RuntimeException {

    public DuplicateClothNameException(String name) {
        super("이미 존재하는 의상 이름입니다: " + name);
    }
}
