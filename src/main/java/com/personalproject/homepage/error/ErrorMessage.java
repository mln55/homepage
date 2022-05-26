package com.personalproject.homepage.error;

import org.springframework.http.HttpStatus;

/********************************************************************************
    상황에 따른 message와 HttpStatus를 저장한다.
    일반적인 경우에는 placeholder "%"를 이용하여 세부 메시지를 설정한다.
********************************************************************************/
public enum ErrorMessage {
    // 4xx
    NOT_ALLOWED_NULL("%은(는) NULL일 수 없습니다.", HttpStatus.BAD_REQUEST),
    NON_EXISTENT("존재하지 않는 %입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTENT("이미 존재하는 %입니다.", HttpStatus.BAD_REQUEST),
    NO_CHANGES("수정할 변경 사항이 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_REMOVEABLE_CATEGORY("카테고리에 속한 포스트가 있어 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 5xx
    INTERNAL_SERVER_ERROR("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
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
