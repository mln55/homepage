package com.personalproject.homepage.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDto {

    private Long id;

    private CategoryDto category;

    private String title;

    private String content;

    private String desc;

    private Long hit;

    private Boolean visible;

    private LocalDateTime postAt;

    private LocalDateTime updateAt;

    private PostDto() {/** empty */}
}
