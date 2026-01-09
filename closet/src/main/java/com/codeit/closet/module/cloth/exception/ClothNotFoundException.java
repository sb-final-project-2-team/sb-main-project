package com.codeit.closet.module.cloth.exception;

import java.util.UUID;

public class ClothNotFoundException extends RuntimeException {

    public ClothNotFoundException(UUID clothId) {

        super("의상을 찾을 수 없습니다. : " + clothId);
    }
}
