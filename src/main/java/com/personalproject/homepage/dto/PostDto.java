package com.personalproject.homepage.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Post entity에 대응하는 DTO 클래스
 */
public class PostDto {

    /**
     * 포스트 요청에 사용되는 DTO 내부 클래스
     */
    @Getter
    public static class Req {
        private Long categoryId;
        private String title;
        private String content;
        private String desc;
        private Boolean visible;
    }

    /**
     * 포스트 응답에 사용되는 DTO 내부 클래스
     */
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Res {
        private Long id;
        private CategoryDto.Res category;
        private String title;
        private String content;
        private String desc;
        private Long hit;
        private Boolean visible;
        private LocalDateTime postAt;
        private LocalDateTime updateAt;
    }
}
