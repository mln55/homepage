package com.personalproject.homepage.error;

import com.personalproject.homepage.api.ApiResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponseEntity {

    public static ResponseEntity<ApiResult<?>> response(String message, HttpStatus status) {
        return new ResponseEntity<ApiResult<?>>(ApiResult.error(message, status), status);
    }
}
