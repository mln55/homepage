package com.personalproject.homepage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryDto {

    private String name;

    private String parent;

    private CategoryDto() {/** empty */}

    @Builder
    private CategoryDto(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }
}
