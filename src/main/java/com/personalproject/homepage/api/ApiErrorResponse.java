package com.personalproject.homepage.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiErrorResponse {

    private final String message;

    private final int status;

    public ApiErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }
}
