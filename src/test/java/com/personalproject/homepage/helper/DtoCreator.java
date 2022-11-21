package com.personalproject.homepage.helper;

import org.springframework.test.util.ReflectionTestUtils;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;


/**
 * 생성자가 보이지 않는 DTO객체를 테스트 환경에서 생성해 주는 클래스
 */
public class DtoCreator {

    /**
     * Create {@link CategoryDto.Req}
     * @param name
     * @param parentId
     * @return {@link CategoryDto.Req}
     */
    public static CategoryDto.Req categoryReqDto(String name, Long parentId) {
        CategoryDto.Req dto = new CategoryDto.Req();
        ReflectionTestUtils.setField(dto, "name", name);
        ReflectionTestUtils.setField(dto, "parentId", parentId);
        return dto;
    }

    /**
     * Create {@link PostDto.Req}
     * @param categoryId
     * @param title
     * @param content
     * @param desc
     * @param visible
     * @return {@link PostDto.Req}
     */
    public static PostDto.Req postReqDto(Long categoryId, String title, String content, String desc, Boolean visible) {
        PostDto.Req dto = new PostDto.Req();
        ReflectionTestUtils.setField(dto, "categoryId", categoryId);
        ReflectionTestUtils.setField(dto, "title", title);
        ReflectionTestUtils.setField(dto, "content", content);
        ReflectionTestUtils.setField(dto, "desc", desc);
        ReflectionTestUtils.setField(dto, "visible", visible);
        return dto;
    }
}
