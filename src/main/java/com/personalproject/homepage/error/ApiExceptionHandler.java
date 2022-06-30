package com.personalproject.homepage.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestController 어노테이션이 붙은 컨트롤러에서 발생한 예외를 처리한다.
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e) {
        return ErrorResponseEntity.response(e.getMessage(), e.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(Exception e) {
        return ErrorResponseEntity.response(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleInternalServerError(Throwable t) {
        ErrorMessage ise = ErrorMessage.INTERNAL_SERVER_ERROR;
        return ErrorResponseEntity.response(ise.getMessage(), ise.getStatus());
    }
}
