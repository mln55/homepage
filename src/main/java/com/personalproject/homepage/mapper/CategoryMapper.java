package com.personalproject.homepage.mapper;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.error.ErrorMessage;

/**
 * Category Entity <--> DTO 객체 변환을 위한 클래스
 */
public class CategoryMapper {

    /**
     * @param entity {@link Category}
     * @return {@link CategoryDto.Res}
     */
    public static CategoryDto.Res entityToResDto(Category entity) {
        checkArgument(entity != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("Category Entity"));

        String name = entity.getName();
        Category parentCategory = entity.getParentCategory();

        return CategoryDto.Res.builder()
            .name(name)
            .parent(parentCategory == null ? null : parentCategory.getName())
            .build();
    }

    /**
     * @param entity {@link Category.WithPostsCount}
     * @return {@link CategoryDto.ResWithPostsCount}
     */
    public static CategoryDto.ResWithPostsCount entityWithPostsCountToResDtoWithPostsCount(Category.WithPostsCount entity) {
        checkArgument(entity != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryWithPostsCount Entity"));

        Category categoryEntity = entity.getCategory();
        Long parentCategoryId = categoryEntity.getParentCategory() == null
            ? null
            : categoryEntity.getParentCategory().getIdx();

        return CategoryDto.ResWithPostsCount.builder()
            .categoryId(categoryEntity.getIdx())
            .name(categoryEntity.getName())
            .parentId(parentCategoryId)
            .postsCount(entity.getPostsCount())
            .build();
    }

    /**
     * @param entityList {@link Category.WithPostsCount} List
     * @return {@link CategoryDto.ResWithPostsCount} List
     */
    public static List<CategoryDto.ResWithPostsCount> entityWithPostsCountListToResDtoWithPostsCountList(List<Category.WithPostsCount> entityList) {
        checkArgument(entityList != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("Category.WithPostsCount EntityList"));

        List<CategoryDto.ResWithPostsCount> dtoList = new ArrayList<>();
        // entity loop
        for (Category.WithPostsCount entity : entityList) {
            Long parentId = entity.getCategory().getParentCategory() == null
                ? null
                : entity.getCategory().getParentCategory().getIdx();

            // if parentId == null -> 상위 카테고리
            if (parentId == null) {
                dtoList.add(CategoryMapper.entityWithPostsCountToResDtoWithPostsCount(entity));

            // if parentId != null -> 하위 카테고리
            } else {
                // child loop
                for (CategoryDto.ResWithPostsCount dto : dtoList) {
                    if (dto.getCategoryId() == parentId) {
                        dto.addChild(CategoryMapper.entityWithPostsCountToResDtoWithPostsCount(entity));
                        break;
                    }
                }
            }
        }

        return dtoList;
    }
}
