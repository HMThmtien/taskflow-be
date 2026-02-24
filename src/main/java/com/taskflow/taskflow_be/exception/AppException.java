package com.taskflow.taskflow_be.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public AppException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
    public AppException(ErrorCode code, HttpStatus status) {
        this(code, status, code.name());
    }

    public AppException(ErrorCode code, String message) {
        this(code, HttpStatus.BAD_REQUEST, message);
    }


    public ErrorCode getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
