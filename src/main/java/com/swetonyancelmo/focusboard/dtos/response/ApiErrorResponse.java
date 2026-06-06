package com.swetonyancelmo.focusboard.dtos.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse withFields(int status, String error, String message, String path, Map<String, String> fields) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, fields);
    }
}
