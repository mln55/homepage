package com.personalproject.homepage.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private HttpStatus status;

    public ApiException(ErrorMessage apiErrorMessage, String... args) {
        super(apiErrorMessage.getMessage(args));
        this.status = apiErrorMessage.getStatus();
    }
}
