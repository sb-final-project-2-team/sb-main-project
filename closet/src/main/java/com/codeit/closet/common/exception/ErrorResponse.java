package com.codeit.closet.common.exception;

import java.util.HashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String exceptionName;
    private final String message;
    private final Object details;
    private final int status;

    public ErrorResponse(Exception exception, int status) {
        this(exception.getClass().getSimpleName(), exception.getMessage(),
                new HashMap<>(), status);
    }
}
