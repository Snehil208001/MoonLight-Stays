package com.moonlight.stays.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MoonlightException extends RuntimeException {
    private final HttpStatus status;

    public MoonlightException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
