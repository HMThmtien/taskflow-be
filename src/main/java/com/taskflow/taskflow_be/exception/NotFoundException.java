package com.taskflow.taskflow_be.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(ErrorCode code) {
        super(code, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ErrorCode code, String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }
}