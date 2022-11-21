package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Controller와 Repository 사이를 연결할 Service 클래스.
 * DTO를 받아 Entity를 반환한다.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리를 생성한다.
     * @param dto {@link CategoryDto.Req}
     * @return {@link Category}
     */
    @Transactional
    public Category createCategory(CategoryDto.Req dto) {
        checkArgument(dto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        checkArgument(StringUtils.hasText(dto.getName()), ErrorMessage.EMPTY_STRING.getMessage("name"));

        // find parent entity
        Category parentEntity = dto.getParentId() == null
            ? null
            : categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리"));

        // 중복 체크
        if (categoryRepository.existsByNameAndParentCategory(dto.getName(), parentEntity)) {
            throw new ApiException(ErrorMessage.ALREADY_EXISTENT, "카테고리");
        }

        // create entity
        Category entity = Category.builder()
            .name(dto.getName())
            .parentCategory(parentEntity)
            .build();

        // save and return
        return categoryRepository.save(entity);
    }

    /**
     * 이름으로 카테고리를 조회한다.
     * @param name 카테고리 이름
     * @param parent 상위 카테고리 이름
     * @return {@link Category}
     */
    @Transactional(readOnly = true)
    public Category getCategory(String name, String parent) {
        // find parent entity
        Category parentEntity = parent == null
            ? null
            : categoryRepository.findByNameAndParentCategory(parent, null)
                .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리"));

        // find entity
        Category entity = categoryRepository.findByNameAndParentCategory(name, parentEntity)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        return entity;
    }

    /**
     * 모든 카테고리를 조회한다.
     * @return {@link Category} List
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 모든 카테고리를 등록된 게시글 수를 포함하여 조회한다.
     * @param visible 포스트 공개 여부, null이면 전부
     * @return {@link Category.WithPostsCount} List
     */
    @Transactional(readOnly = true)
    public List<Category.WithPostsCount> getAllCategoriesWithPostsCount(Boolean visible) {
        return categoryRepository.allCategoriesWithPostsCount(visible);
    }


    /**
     * 카테고리 정보를 수정한다.
     * @param categoryId 카테고리 아이디
     * @param dto {@link CategoryDto.Req}
     * @return {@link Category}
     */
    @Transactional
    public Category updateCategory(Long categoryId, CategoryDto.Req dto) {
        // name == null -> name 변경 X
        checkArgument(dto.getName() == null || dto.getName().trim().length() > 0,
            ErrorMessage.EMPTY_STRING.getMessage("name"));

        // find entity
        Category entity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        // find parent entity
        Category parentEntity = dto.getParentId() == null
            ? null
            : categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리"));

        // 중복 체크
        // dto.name == null 인 경우 entity.name으로 검색한다.
        String name = dto.getName() == null ? entity.getName() : dto.getName();
        if (categoryRepository.existsByNameAndParentCategory(name, parentEntity)) {
            throw new ApiException(ErrorMessage.ALREADY_EXISTENT, "카테고리");
        }

        // update entity
        entity.updateInfo(dto.getName(), parentEntity);

        // return entity
        return entity;
    }

    /**
     * 카테고리를 삭제한다.
     * @param categoryId 카테고리 아이디
     * @return {@code true}
     */
    @Transactional
    public boolean deleteCategory(Long categoryId) {

        // find entity
        Category entity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        // check having posts
        checkArgument(entity.getPostsOfCategory().size() == 0, ErrorMessage.NOT_REMOVEABLE_CATEGORY.getMessage());
        for (Category childEntity : entity.getCategoriesOfCategory()) {
            checkArgument(childEntity.getPostsOfCategory().size() == 0, ErrorMessage.NOT_REMOVEABLE_CATEGORY.getMessage());
        }

        // delete
        categoryRepository.delete(entity);
        return true;
    }
}
