package com.personalproject.homepage.mapper;

import static com.google.common.base.Preconditions.checkArgument;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.repository.PostRepository;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostRepository postRepository;

    private final CategoryMapper categoryMapper;

    public PostDto entityToPostDto(Post entity) {
        checkArgument(entity != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("Post Entity"));
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
        checkArgument(postDto != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("PostDto"));
        Post entity;
        Long id = postDto.getId();

        // id == null -> 새로운 entity를 생성한다.
        if (id == null) {
            CategoryDto categoryDto = postDto.getCategory();
            Category category = categoryDto == null ? null : categoryMapper.CategoryDtoToEntity(categoryDto);
            if (category != null) {
                checkArgument(category.getIdx() != null, ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
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
                () -> new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
            );
        }
        return entity;
    }

}