package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.repository.CategoryRepository;
import com.personalproject.homepage.repository.PostRepository;

import lombok.RequiredArgsConstructor;
/**
 * Controller와 Repository 사이를 연결할 Service 클래스.
 * DTO를 받아 Entity를 반환한다.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    /**
     * 포스트를 생성한다.
     * @param postDto {@link PostDto.Req}
     * @return {@link Post}
     */
    @Transactional
    public Post createPost(PostDto.Req postDto) {
        checkArgument(postDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("PostDto"));
        checkArgument(postDto.getCategoryId() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("categoryId"));
        checkArgument(postDto.getTitle() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("title"));
        checkArgument(postDto.getContent() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("content"));
        checkArgument(postDto.getDesc() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("desc"));
        checkArgument(postDto.getVisible() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));

        // find category entity
        Category category = categoryRepository.findById(postDto.getCategoryId())
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        // create entity
        Post entity = Post.builder()
            .category(category)
            .title(postDto.getTitle())
            .content(postDto.getContent())
            .desc(postDto.getDesc())
            .visible(postDto.getVisible())
            .build();

        // save and return
        return postRepository.save(entity);
    }

    /**
     * id로 포스트를 조회한다.
     * @param id 포스트 아이디
     * @return {@link Post}
     */
    @Transactional
    public Post getPost(Long id) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));

        // find entity
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );

        entity.addHit(); // 단순 요청 시 조회수 +1
        return entity;
    }

    /**
     * 포스트를 페이지에 맞게 조회한다.
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Transactional(readOnly = true)
    public List<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * 포스트를 카테고리, 페이지에 맞게 조회한다.
     * @param categoryId 카테고리 아이디
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
        // find category entity
        Category categoryEntity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        return categoryEntity.getParentCategory() == null
            ? postRepository.findAllIncludeChildCategory(categoryEntity, pageable)
            : postRepository.findAllByCategory(categoryEntity, pageable);
    }


    /**
     * 포스트를 공개 여부, 페이지에 맞게 조회한다.
     * @param visible 공개 여부
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByVisible(Boolean visible, Pageable pageable) {
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));
        return postRepository.findAllByVisible(visible, pageable);
    }

    /**
     * 포스트를 카테고리, 공개 여부, 페이지에 맞게 조회한다.
     * @param visible 공개 여부
     * @param categoryId 카테고리 아이디
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByVisibleAndCategory(Boolean visible, Long categoryId, Pageable pageable) {
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));

        // find category entity
        Category categoryEntity = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        return categoryEntity.getParentCategory() == null
            ? postRepository.findAllVisibleIncludeChildCategory(visible, categoryEntity, pageable)
            : postRepository.findAllByVisibleAndCategory(visible, categoryEntity, pageable);
    }

    /**
     * 포스트를 카테고리, 공개 여부, 페이지에 맞게 조회한다.
     * @param visible 공개 여부
     * @param categoryDto {@link CategoryDto.NameReq}
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByVisibleAndCategory(Boolean visible, CategoryDto.NameReq categoryDto, Pageable pageable) {
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));

        Category parentCategoryEntity = categoryDto.getParent() == null
            ? null
            : categoryRepository.findByNameAndParentCategory(categoryDto.getParent(), null)
                .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리"));
        Category categoryEntity = categoryRepository.findByNameAndParentCategory(categoryDto.getName(), parentCategoryEntity)
            .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        return categoryEntity.getParentCategory() == null
                ? postRepository.findAllVisibleIncludeChildCategory(visible, categoryEntity, pageable)
                : postRepository.findAllByVisibleAndCategory(visible, categoryEntity, pageable);
    }

    /**
     * 포스트를 수정한다.
     * @param id 포스트 아이디
     * @param dto PostDto.Req
     * @return Post
     */
    @Transactional
    public Post updatePost(Long id, PostDto.Req dto) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));

        // find entity
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );

        // find category entity
        Long categoryId = dto.getCategoryId();
        Category category = categoryId == null || categoryId == entity.getCategory().getIdx()
            ? null
            : categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

        // update
        entity.updateInfo(category, dto.getTitle(), dto.getContent(), dto.getDesc(), dto.getVisible());

        // return
        return entity;
    }

    /**
     * 포스트를 삭제한다.
     * @param id 포스트 아이디
     * @return {@code true}
     */
    @Transactional
    public boolean deletePost(Long id) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));

        // find entity
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );

        // delete
        postRepository.delete(entity);
        return true;
    }
}
