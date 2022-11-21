package com.personalproject.homepage.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.service.CategoryService;
import com.personalproject.homepage.util.AppUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;

    /**
     * 모든 카테고리를 반환한다.
     * @return {@link CategoryDto.Res} List
     */
    @GetMapping("")
    public ApiResult<List<CategoryDto.Res>> getCategories() {
        List<CategoryDto.Res> dtoList = categoryService.getAllCategories()
            .stream()
            .map(CategoryMapper::entityToResDto)
            .collect(Collectors.toList());

        return ApiResult.success(dtoList);
    }

    /**
     * 모든 카테고리를 등록된 게시글 수와 함께 반환한다.
     * @param count query parameter, required
     * @param strVisible query parameter
     * @return {@link CategoryDto.ResWithPostsCount}
     */
    @GetMapping(path = "", params = "count")
    public ApiResult<List<CategoryDto.ResWithPostsCount>> getCategoriesWithCount(
        @RequestParam String count,
        @RequestParam(value = "visible", required = false) String strVisible
    ) {
        // post의 count
        if ("post".equals(count)) {
            List<Category.WithPostsCount> entityList = categoryService.getAllCategoriesWithPostsCount(AppUtils.parseBoolean(strVisible));
            List<CategoryDto.ResWithPostsCount> dtoList = CategoryMapper.entityWithPostsCountListToResDtoWithPostsCountList(entityList);
            return ApiResult.success(dtoList);

        // 아닐 경우 throw
        } else {
            throw new ApiException(ErrorMessage.INVALID_PARAM_INFO, "count", "post");
        }
    }

    /**
     * 카테고리를 생성하고 결과를 반환한다.
     * @param dto RequestBody {@link CategoryDto.Req}
     * @return {@link CategoryDto.Res}
     */
    @PostMapping("")
    public ApiResult<CategoryDto.Res> createCategory(@RequestBody CategoryDto.Req dto) {
        Category entity = categoryService.createCategory(dto);
        return ApiResult.success(CategoryMapper.entityToResDto(entity));
    }

    /**
     * 카테고리를 수정하고 결과를 반환한다.
     * @param id 카테고리 아이디
     * @param dto {@link CategoryDto.Req}
     * @return {@link CategoryDto.Res}
     */
    @PatchMapping({"/{id}"})
    public ApiResult<CategoryDto.Res> updateCategory(
        @PathVariable String id,
        @RequestBody CategoryDto.Req dto
    ) {
        Category entity = categoryService.updateCategory(AppUtils.parseParamId(id), dto);
        return ApiResult.success(CategoryMapper.entityToResDto(entity));
    }

    /**
     * 카테고리를 삭제하고 결과를 반환한다.
     * @param id 카테고리 아이디
     * @return Boolean, {@code true}
     */
    @DeleteMapping({"/{id}"})
    public ApiResult<Boolean> deleteCategory(@PathVariable String id) {
        return ApiResult.success(categoryService.deleteCategory(AppUtils.parseParamId(id)));
    }
}
