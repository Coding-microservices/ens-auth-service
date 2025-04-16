package io.vladprotchenko.authapi.exception;

import lombok.Getter;

@Getter
public class CustomFeignException extends RuntimeException {

    private final int statusCode;

    public CustomFeignException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
