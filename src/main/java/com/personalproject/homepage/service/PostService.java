package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
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
        checkArgument(postDto != null, "PostDto는 null일 수 없습니다.");
        checkArgument(postDto.getTitle() != null, "title은 null일 수 없습니다.");
        checkArgument(postDto.getContent() != null, "content는 null일 수 없습니다.");
        checkArgument(postDto.getVisible() != null, "visible은 null일 수 없습니다.");

        Post entity = postMapper.postDtoToEntity(postDto);
        return postMapper.entityToPostDto(postRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long id) {
        checkArgument(id != null, "포스트의 id는 null일 수 없습니다.");
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 포스트입니다.")
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
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");
        Category categoryEntity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(categoryEntity.getIdx() != null, "존재하지 않는 카테고리입니다.");
        return postRepository.findAllByCategory(categoryEntity, pageable)
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    public List<PostDto> getVisiblePosts(Pageable pageable) {
        return postRepository.findAllByVisibleTrue(pageable)
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    public List<PostDto> getVisiblePostsByCategory(CategoryDto categoryDto, Pageable pageable) {
        checkArgument(categoryDto != null, "CategoryDto는 null일 수 없습니다.");
        Category categoryEntity = categoryMapper.CategoryDtoToEntity(categoryDto);
        checkArgument(categoryEntity.getIdx() != null, "존재하지 않는 카테고리입니다.");
        return postRepository.findAllByVisibleTrueAndCategory(categoryEntity, pageable)
            .stream()
            .map(postMapper::entityToPostDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public PostDto updatePost(Long id, PostDto postDto) {
        checkArgument(id != null, "id는 null일 수 없습니다.");
        Post entityBefore = postRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 포스트입니다.")
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
        checkArgument(updatable, "수정할 변경 사항이 없습니다.");

        entityBefore.updateInfo(categoryAfter, titleAfter, contentAfter, visibleAfter);

        return postMapper.entityToPostDto(entityBefore);
    }

    public boolean deletePost(Long id) {
        checkArgument(id != null, "id는 null일 수 없습니다.");
        Post entity = postRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 포스트입니다.")
        );
        postRepository.delete(entity);
        return true;
    }
}
