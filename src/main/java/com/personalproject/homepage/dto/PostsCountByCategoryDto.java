package com.personalproject.homepage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsCountByCategoryDto {

    private CategoryDto category;

    private Long count;

    @Builder
    public PostsCountByCategoryDto (CategoryDto categoryDto, Long count) {
        this.category = categoryDto;
        this.count = count;
    }
}
