package com.personalproject.homepage.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;

// api 요청의 응답으로 쓰일 객체
@Getter
public class ApiResult<T> {

    private final boolean success;
    private final T response;
    private final ApiErrorResponse error;

    private ApiResult(boolean success, T response, ApiErrorResponse error) {
        this.success = success;
        this.response = response;
        this.error = error;
    }

    public static <T> ApiResult<T> success(T response) {
        return new ApiResult<>(true, response, null);
    }

    public static ApiResult<?> error(String message, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiErrorResponse(message, status));
    }
}
