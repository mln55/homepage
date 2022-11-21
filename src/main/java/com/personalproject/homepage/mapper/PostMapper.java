package com.personalproject.homepage.mapper;

import static com.google.common.base.Preconditions.checkArgument;

import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ErrorMessage;

/**
 * Post Entity <--> DTO 객체 변환을 위한 클래스
 */
public class PostMapper {

    /**
     * @param entity {@link Post}
     * @return {@link PostDto.Res}
     */
    public static PostDto.Res entityToResDto(Post entity) {
        checkArgument(entity != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("Post Entity"));

        return PostDto.Res.builder()
            .id(entity.getIdx())
            .category(CategoryMapper.entityToResDto(entity.getCategory()))
            .title(entity.getTitle())
            .content(entity.getContent())
            .desc(entity.getDesc())
            .hit(entity.getHit())
            .visible(entity.getVisible())
            .postAt(entity.getCreateAt())
            .updateAt(entity.getUpdateAt())
            .build();
    }
}
