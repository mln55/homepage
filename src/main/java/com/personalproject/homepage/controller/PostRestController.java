package com.personalproject.homepage.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.mapper.PostMapper;
import com.personalproject.homepage.service.PostService;
import com.personalproject.homepage.util.AppUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostRestController {

    private final PostService postService;

    /**
     * 모든 포스트를 요청에 맞게 반환한다.
     * @param pageable 페이지
     * @param strCategoryId 등록된 카테고리 아이디
     * @param strVisible 공개 여부
     * @return {@link PostDto.Res} List
     */
    @GetMapping("")
    public ApiResult<List<PostDto.Res>> getPosts(
        Pageable pageable,
        @RequestParam(value = "categoryid", required = false) String strCategoryId,
        @RequestParam(value = "visible", required = false) String strVisible
    ) {
        Long categoryId = strCategoryId == null ? null : AppUtils.parseParamId(strCategoryId);
        Boolean visible = AppUtils.parseBoolean(strVisible);
        List<Post> entityList = null;
        if (visible == null) {
            entityList = categoryId == null
                ? postService.getPosts(pageable)
                : postService.getPostsByCategory(categoryId, pageable);
        } else {
            entityList = categoryId == null
                ? postService.getPostsByVisible(visible, pageable)
                : postService.getPostsByVisibleAndCategory(visible, categoryId, pageable);
        }
        List<PostDto.Res> dtoList = entityList.stream()
            .map(PostMapper::entityToResDto)
            .collect(Collectors.toList());
        return ApiResult.success(dtoList);
    }

    /**
     * 포스트를 생성하고 결과를 반환한다.
     * @param post {@link PostDto.Req}
     * @return {@link PostDto.Res}
     */
    @PostMapping("")
    public ApiResult<PostDto.Res> createPost(@RequestBody(required = false) PostDto.Req post) {
        Post entity = postService.createPost(post);
        return ApiResult.success(PostMapper.entityToResDto(entity));
    }

    /**
     * id에 맞는 포스트를 반환한다.
     * @param id 포스트 id
     * @return {@link PostDto.Res}
     */
    @GetMapping("/{id}")
    public ApiResult<PostDto.Res> getPost(@PathVariable String id) {
        Post entity = postService.getPost(AppUtils.parseParamId(id));
        return ApiResult.success(PostMapper.entityToResDto(entity));
    }

    /**
     * id에 맞는 포스트를 수정한다.
     * @param id 포스트 id
     * @param post {@link PostDto.Req}
     * @return {@link PostDto.Res}
     */
    @PatchMapping("/{id}")
    public ApiResult<PostDto.Res> updatePost(
        @PathVariable String id,
        @RequestBody(required = false) PostDto.Req post
    ) {
        Post entity = postService.updatePost(AppUtils.parseParamId(id), post);
        return ApiResult.success(PostMapper.entityToResDto(entity));
    }

    /**
     * id에 맞는 포스트를 삭제한다.
     * @param id 포스트 아이디
     * @return Boolean, {@code true}
     */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> deletePost(@PathVariable String id) {
        return ApiResult.success(postService.deletePost(AppUtils.parseParamId(id)));
    }
}
