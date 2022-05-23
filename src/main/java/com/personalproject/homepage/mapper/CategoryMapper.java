package com.personalproject.homepage.mapper;

import static com.google.common.base.Preconditions.checkArgument;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.repository.CategoryRepository;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final CategoryRepository categoryRepository;

    public CategoryDto entityToCategoryDto(Category entity) {
        checkArgument(entity != null, "Category Entity는 null일 수 없습니다.");
        String name = entity.getName();
        Category parentCategory = entity.getParentCategory();
        return CategoryDto.builder()
            .name(name)
            .parent(parentCategory == null ? null : parentCategory.getName())
            .build();
    }

    /********************************************************************************
        CategoryDto에 idx가 포함되지 않기에 find 후 존재하면 idx가 있는 entity를,
        존재하지 않으면 idx가 없는 새로운 entity를 생성 해 반환한다.
    ********************************************************************************/
    public Category CategoryDtoToEntity(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");
        String name = categoryDto.getName();
        String parentCategoryName = categoryDto.getParent();
        Category mappedEntity;
        if (parentCategoryName == null) {
            mappedEntity = categoryRepository.findByNameAndParentCategory(name, null).orElseGet(
                () -> Category.builder().name(name).build()
            );
        } else {
            Category parentCategory = categoryRepository.findByNameAndParentCategory(parentCategoryName, null).orElseThrow(
                () -> new IllegalArgumentException("상위 카테고리가 존재하지 않습니다.")
            );
            mappedEntity = categoryRepository.findByNameAndParentCategory(name, parentCategory).orElseGet(
                () -> Category.builder().name(name).parentCategory(parentCategory).build()
            );
        }
        return mappedEntity;
    }
}
