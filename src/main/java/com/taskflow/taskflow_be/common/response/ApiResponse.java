package com.taskflow.taskflow_be.common.response;

import java.time.Instant;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;
    private Instant timestamp = Instant.now();
    private String requestId;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return fail(code, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message, Object details, String requestId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = new ApiError(code, message, details);
        r.requestId = requestId;
        return r;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ApiError getError() { return error; }
    public Instant getTimestamp() { return timestamp; }
    public String getRequestId() { return requestId; }

    public record ApiError(String code, String message, Object details) {}
}
