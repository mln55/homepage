package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");
        checkArgument(categoryDto.getName() != null, "name은 null일 수 없습니다.");

        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() == null, "이미 존재하는 카테고리입니다.");

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
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");
        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() != null, "존재하지 않는 카테고리입니다.");

        return entity.getCategoriesOfCategory()
            .stream()
            .map(categoryMapper::entityToCategoryDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto before, CategoryDto after) {
        checkArgument(before != null && after != null, "CategoryDto는 null일 수 없습니다.");
        String beforeName = before.getName();
        String beforeParentName = before.getParent();
        String afterName = after.getName();
        String afterParentName = after.getParent();

        Category entityBefore = categoryMapper.CategoryDtoToEntity(before);
        checkArgument(entityBefore.getIdx() != null, "존재하지 않는 카테고리입니다.");

        boolean updatable = (afterName != null && !afterName.equals(beforeName))
            || (afterParentName != null && !afterParentName.equals(beforeParentName))
            || (beforeParentName != null && !beforeParentName.equals(afterParentName));
        checkArgument(updatable, "수정할 변경 사항이 없습니다.");

        Category entityAfter = categoryMapper.CategoryDtoToEntity(after);
        checkArgument(entityAfter.getIdx() == null, "이미 존재하는 카테고리입니다.");

        entityBefore.updateInfo(entityAfter.getName(), entityAfter.getParentCategory());
        return categoryMapper.entityToCategoryDto(entityBefore);
    }

    @Transactional
    public boolean deleteCategory(CategoryDto categoryDto) {
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");

        Category entity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(entity.getIdx() != null, "존재하지 않는 카테고리입니다.");
        checkArgument(entity.getPostsOfCategory().size() == 0, "카테고리에 속한 포스트가 있어 삭제할 수 없습니다.");
        categoryRepository.delete(entity);
        return true;
    }
}
