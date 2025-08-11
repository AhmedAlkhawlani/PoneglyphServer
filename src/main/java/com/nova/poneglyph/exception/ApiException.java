package com.nova.poneglyph.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String detail;

    public ApiException(HttpStatus status, ErrorCode errorCode, String detail) {
        super(detail);
        this.status = status;
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
