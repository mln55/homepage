package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.dto.PostsCountByCategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.mapper.PostMapper;
import com.personalproject.homepage.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final PostMapper postMapper;

    private final CategoryMapper categoryMapper;

    public PostDto createPost(PostDto postDto) {
        checkArgument(postDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("PostDto"));
        checkArgument(postDto.getTitle() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("title"));
        checkArgument(postDto.getContent() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("content"));
        checkArgument(postDto.getVisible() != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));

        Post entity = postMapper.postDtoToEntity(postDto);
        return postMapper.entityToPostDto(postRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long id) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );
        entity.addHit(); // 단순 요청 시 조회수 +1
        return postMapper.entityToPostDto(entity);
    }

    public List<PostDto> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    public List<PostDto> getPostsByCategory(CategoryDto categoryDto, Pageable pageable) {
        checkArgument(categoryDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        Category categoryEntity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(categoryEntity.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        return (
            categoryEntity.getParentCategory() == null
                ? postRepository.findAllByCategory_ParentCategory(categoryEntity, pageable)
                : postRepository.findAllByCategory(categoryEntity, pageable)
            )
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    public List<PostDto> getPostsByVisible(Boolean visible, Pageable pageable) {
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));
        return postRepository.findAllByVisible(visible, pageable)
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    public List<PostDto> getPostsByVisibleAndCategory(Boolean visible, CategoryDto categoryDto, Pageable pageable) {
        checkArgument(categoryDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("CategoryDto"));
        Category categoryEntity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(categoryEntity.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));
        return (
                categoryEntity.getParentCategory() == null
                    ? postRepository.findAllByVisibleAndCategory_ParentCategory(visible, categoryEntity, pageable)
                    : postRepository.findAllByVisibleAndCategory(visible, categoryEntity, pageable)
            )
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostsCountByCategoryDto> getPostsCountPerCategory() {
        return postRepository.countAllGroupByCategory()
            .stream()
            .map(pc -> PostsCountByCategoryDto.builder()
                .categoryDto(categoryMapper.entityToCategoryDto(pc.getCategory()))
                .count(pc.getCount())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostsCountByCategoryDto> getPostsCountByVisiblePerCategory(Boolean visible) {
        checkArgument(visible != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("visible"));
        return postRepository.countAllByVisibleGroupByCategory(visible)
            .stream()
            .map(pc -> PostsCountByCategoryDto.builder()
                .categoryDto(categoryMapper.entityToCategoryDto(pc.getCategory()))
                .count(pc.getCount())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public PostDto updatePost(Long id, PostDto postDto) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));
        Post entityBefore = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );
        Post entityAfter = postMapper.postDtoToEntity(postDto);
        Category categoryBefore = entityBefore.getCategory();
        Category categoryAfter = entityAfter.getCategory();
        String titleAfter = entityAfter.getTitle();
        String contentAfter = entityAfter.getContent();
        Boolean visibleAfter = entityAfter.getVisible();

        boolean updatable = (titleAfter != null && !titleAfter.equals(entityBefore.getTitle()))
            || (visibleAfter != null && visibleAfter != entityBefore.getVisible())
            || (contentAfter != null && !contentAfter.equals(entityBefore.getContent()))
            || (categoryBefore != null && !categoryBefore.equals(categoryAfter))
            || (categoryAfter != null && !categoryAfter.equals(categoryBefore));
        checkArgument(updatable, ErrorMessage.NO_CHANGES.getMessage());

        entityBefore.updateInfo(categoryAfter, titleAfter, contentAfter, visibleAfter);

        return postMapper.entityToPostDto(entityBefore);
    }

    public boolean deletePost(Long id) {
        checkArgument(id != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("id"));
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
        );
        postRepository.delete(entity);
        return true;
    }
}
