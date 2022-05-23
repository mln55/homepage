package com.personalproject.homepage.mapper;

import static com.google.common.base.Preconditions.checkArgument;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.repository.PostRepository;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostRepository postRepository;

    private final CategoryMapper categoryMapper;

    public PostDto entityToPostDto(Post entity) {
        checkArgument(entity != null, "Post Entity는 null일 수 없습니다.");
        Category category = entity.getCategory();
        CategoryDto categoryDto = category == null ? null : categoryMapper.entityToCategoryDto(category);
        return PostDto.builder()
            .id(entity.getIdx())
            .category(categoryDto)
            .title(entity.getTitle())
            .content(entity.getContent())
            .hit(entity.getHit())
            .visible(entity.getVisible())
            .postAt(entity.getCreateAt())
            .updateAt(entity.getUpdateAt())
            .build();
    }

    public Post postDtoToEntity(PostDto postDto) {
        checkArgument(postDto != null, "PostDto는 null일 수 없습니다.");
        Post entity;
        Long id = postDto.getId();

        // id == null -> 새로운 entity를 생성한다.
        if (id == null) {
            CategoryDto categoryDto = postDto.getCategory();
            Category category = categoryDto == null ? null : categoryMapper.CategoryDtoToEntity(categoryDto);
            if (category != null) {
                checkArgument(category.getIdx() != null, "존재하지 않는 카테고리입니다.");
            }
            entity = Post.builder()
                .category(category)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .visible(postDto.getVisible())
                .build();

        // id != null -> 저장된 entity를 조회한다.
        } else {
            entity = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 포스트입니다.")
            );
        }
        return entity;
    }

}
