package com.personalproject.homepage.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostDto {

    private Long id;

    private CategoryDto category;

    private String title;

    private String content;

    private Long hit;

    private Boolean visible;

    private LocalDateTime postAt;

    private LocalDateTime updateAt;

    private PostDto() {/** empty */}

    @Builder
    private PostDto(Long id, CategoryDto category, String title, String content, Long hit, Boolean visible, LocalDateTime postAt, LocalDateTime updateAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.content = content;
        this.hit = hit;
        this.visible = visible;
        this.postAt = postAt;
        this.updateAt = updateAt;
    }
}
