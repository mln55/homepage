package com.personalproject.homepage.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

/********************************************************************************
    application에서 발생한 예외를 처리한다.
    각 ExceptionHandler의 @ControllerAdvice는 annotations값이 설정 되어있다.
    controller의 메소드가 실행 되기 전에 발생한 예외는 처리하지 못하므로
    이 클래스에서 처리하도록 한다.
********************************************************************************/
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        ErrorMessage methodNotAllowed = ErrorMessage.METHOD_NOT_ALLOWED;
        String message = methodNotAllowed.getMessage(e.getMethod(), String.join(", ", e.getSupportedMethods()));
        return ErrorResponseEntity.response(message, methodNotAllowed.getStatus());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(NoHandlerFoundException e) {
        String url = e.getRequestURL();
        if (url.startsWith("/api/")) {
            return handleNotFoundApi();
        } else {
            /** todo */
            return null;
        }
    }

    public ResponseEntity<?> handleNotFoundApi() {
        ErrorMessage notFound = ErrorMessage.API_NOT_FOUND;
        return ErrorResponseEntity.response(notFound.getMessage(), notFound.getStatus());
    }
}
