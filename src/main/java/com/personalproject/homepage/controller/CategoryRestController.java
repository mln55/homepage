package com.personalproject.homepage.controller;

import java.util.List;

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.service.CategoryService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;

    @GetMapping("")
    public ApiResult<List<CategoryDto>> getCategories(
        @RequestParam(required = false) String lvl,
        @RequestParam(required = false) String name
    ) {
        return ApiResult.success(invokeGetCategories(lvl, name));
    }

    @PostMapping("")
    public ApiResult<CategoryDto> createCategory(@RequestBody(required = false) CategoryDto categoryDto) {
        return ApiResult.success(categoryService.createCategory(categoryDto));
    }

    @PatchMapping({"/{name}", "/{parent}/{name}"})
    public ApiResult<CategoryDto> updateCategory(
        @PathVariable(required = false) String parent,
        @PathVariable String name,
        @RequestBody(required = false) CategoryDto afterCategory
    ) {
        CategoryDto beforeCategory = CategoryDto.builder().name(name).parent(parent).build();
        return ApiResult.success(categoryService.updateCategory(beforeCategory, afterCategory));
    }

    @DeleteMapping({"/{name}", "/{parent}/{name}"})
    public ApiResult<Boolean> deleteCategory(
        @PathVariable(required = false) String parent,
        @PathVariable String name
    ) {
        CategoryDto category = CategoryDto.builder().name(name).parent(parent).build();
        return ApiResult.success(categoryService.deleteCategory(category));
    }

    private List<CategoryDto> invokeGetCategories(String lvl, String name) {
        List<CategoryDto> categoryList;
        if (lvl == null) {
            categoryList = categoryService.getAllCategories();
        } else if (lvl.equals("top")) {
            categoryList = categoryService.getAllTopLevelCategories();
        } else if (lvl.equals("sub")) {
            if (name == null || name.trim().length() == 0) {
                throw new ApiException(ErrorMessage.EMPTY_QUERY_STRING, "name");
            }
            CategoryDto category = CategoryDto.builder().name(name).build();
            categoryList = categoryService.getAllSubCategoriesOf(category);
        } else {
            throw new ApiException(ErrorMessage.INVALID_QUERY_STRING, "lvl", "top, sub");
        }
        return categoryList;
    }
}
