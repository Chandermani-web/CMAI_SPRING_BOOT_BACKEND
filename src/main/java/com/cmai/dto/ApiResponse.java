package com.cmai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Common success envelope. Null fields are left out of the JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, String source, Integer count, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, null, null, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data, String source) {
        return new ApiResponse<>(true, message, source, null, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data, String source, Integer count) {
        return new ApiResponse<>(true, message, source, count, data);
    }
}
