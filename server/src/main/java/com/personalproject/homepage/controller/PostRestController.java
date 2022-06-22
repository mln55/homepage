package com.personalproject.homepage.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.service.PostService;

import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostRestController {

    private final PostService postService;

    @GetMapping("")
    public ApiResult<List<PostDto>> getPosts(
        Pageable pageable,
        CategoryDto category,
        @RequestParam(required = false) String visible
    ) {
        return ApiResult.success(invokeGetPosts(pageable, category, visible));
    }

    @PostMapping("")
    public ApiResult<PostDto> createPost(@RequestBody(required = false) PostDto post) {
        return ApiResult.success(postService.createPost(post));
    }

    @GetMapping("/{id}")
    public ApiResult<PostDto> getPost(@PathVariable String id) {
        return ApiResult.success(postService.getPost(parseId(id)));
    }

    @PatchMapping("/{id}")
    public ApiResult<PostDto> updatePost(
        @PathVariable String id,
        @RequestBody(required = false) PostDto post
    ) {
        return ApiResult.success(postService.updatePost(parseId(id), post));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> deletePost(@PathVariable String id) {
        return ApiResult.success(postService.deletePost(parseId(id)));
    }

    private List<PostDto> invokeGetPosts(
        Pageable pageable,
        CategoryDto category,
        @RequestParam String visible
    ) {
        if (visible == null) {
            return category.getName() == null
                ? postService.getPosts(pageable)
                : postService.getPostsByCategory(category, pageable);
        } else {
            Boolean boolVisible = "true".equalsIgnoreCase(visible) ? Boolean.TRUE
                : "false".equalsIgnoreCase(visible) ? Boolean.FALSE
                : null;
            checkArgument(boolVisible != null, ErrorMessage.INVALID_QUERY_STRING.getMessage("visible", "true, false"));
            return category.getName() == null
                ? postService.getPostsByVisible(boolVisible, pageable)
                : postService.getPostsByVisibleAndCategory(boolVisible, category, pageable);
        }
    }

    private long parseId(String idStr) {
        try {
            float idFloat = Float.parseFloat(idStr);
            checkArgument(idFloat == Math.floor(idFloat) && idFloat > 0 && idFloat <= Long.MAX_VALUE,
                ErrorMessage.INVALID_PATH_PARAM.getMessage("id"));
            return (long) idFloat;
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorMessage.INVALID_PATH_PARAM, "id");
        }
    }
}
