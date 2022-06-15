package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        checkArgument(categoryDto.getName() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("name"));

        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() == null, ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));

        return categoryMapper.entityToCategoryDto(categoryRepository.save(entity));
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(categoryMapper::entityToCategoryDto)
            .collect(Collectors.toList());
    }

    public List<CategoryDto> getAllTopLevelCategories() {
        return categoryRepository.findAllByParentCategoryIsNull()
            .stream()
            .map(categoryMapper::entityToCategoryDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllSubCategoriesOf(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));

        return entity.getCategoriesOfCategory()
            .stream()
            .map(categoryMapper::entityToCategoryDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto before, CategoryDto after) {
        checkArgument(before != null && after != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        String beforeName = before.getName();
        String beforeParentName = before.getParent();
        String afterName = after.getName();
        String afterParentName = after.getParent();

        Category entityBefore = categoryMapper.CategoryDtoToEntity(before);
        checkArgument(entityBefore.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));

        if (entityBefore.getPostsOfCategory().size() != 0 && after.getParent() == null) {
            throw new ApiException(ErrorMessage.NOT_CHANGE_TO_TOPLEVEL_CATEGORY);
        }

        boolean updatable = (afterName != null && !afterName.equals(beforeName))
            || (beforeParentName != null && !beforeParentName.equals(afterParentName))
            || (afterParentName != null && !afterParentName.equals(beforeParentName));
        checkArgument(updatable, ErrorMessage.NO_CHANGES.getMessage());

        Category entityAfter = categoryMapper.CategoryDtoToEntity(after);
        checkArgument(entityAfter.getIdx() == null, ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));

        entityBefore.updateInfo(entityAfter.getName(), entityAfter.getParentCategory());
        return categoryMapper.entityToCategoryDto(entityBefore);
    }

    @Transactional
    public boolean deleteCategory(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("카테고리"));

        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        checkArgument(entity.getPostsOfCategory().size() == 0, ErrorMessage.NOT_REMOVEABLE_CATEGORY.getMessage());
        categoryRepository.delete(entity);
        return true;
    }
}
