package com.personalproject.homepage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsCountByCategoryDto {

    private CategoryDto category;

    private Long visibleCount;

    private Long invisibleCount;

    @Builder
    public PostsCountByCategoryDto (CategoryDto categoryDto, Long visibleCount, Long invisibleCount) {
        this.category = categoryDto;
        this.visibleCount = visibleCount;
        this.invisibleCount = invisibleCount;
    }
}
