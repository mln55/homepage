package com.personalproject.homepage.error;

import org.springframework.http.HttpStatus;

/********************************************************************************
    상황에 따른 message와 HttpStatus를 저장한다.
    일반적인 경우에는 placeholder "%"를 이용하여 세부 메시지를 설정한다.
********************************************************************************/
public enum ErrorMessage {
    // 400
    NOT_ALLOWED_NULL("%은(는) NULL일 수 없습니다.", HttpStatus.BAD_REQUEST),
    NON_EXISTENT("존재하지 않는 %입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTENT("이미 존재하는 %입니다.", HttpStatus.BAD_REQUEST),
    NO_CHANGES("수정할 변경 사항이 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_REMOVEABLE_CATEGORY("카테고리에 속한 포스트가 있어 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    EMPTY_QUERY_STRING("query string %이(가) 비어있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_QUERY_STRING("%의 값이 유효하지 않습니다. 유효한 값: %", HttpStatus.BAD_REQUEST),
    INVALID_PATH_PARAM("%의 값이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
    JWT_EXPIERD("만료된 토큰입니다.", HttpStatus.BAD_REQUEST),
    JWT_INVALID("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),

    // 401
    UNAUTHORIZED("인증이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED),

    // 403
    ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404
    API_NOT_FOUND("존재하지 않는 URL입니다.", HttpStatus.NOT_FOUND),

    // 405
    METHOD_NOT_ALLOWED("%메소드는 지원하지 않습니다. 지원하는 메소드: %", HttpStatus.METHOD_NOT_ALLOWED),

    // 500
    INTERNAL_SERVER_ERROR("알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private String message;
    private HttpStatus status;

    private ErrorMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage(String... args) {
        return parseMessage(this.message, args);
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    private String parseMessage(String message, String... args) {
        if (message == null || message.trim().length() == 0 || args == null || args.length == 0) {
            return message;
        }
        for (String arg : args) {
            message = message.replaceFirst("%", arg);
        }
        return message;
    }
}
