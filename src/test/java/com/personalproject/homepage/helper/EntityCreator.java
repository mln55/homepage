package com.personalproject.homepage.helper;

import org.springframework.test.util.ReflectionTestUtils;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;

/**
 * 테스트 환경에서 Entity 객체를 idx를 포함하여 생성해 주는 클래스
 */
public class EntityCreator {

    /**
     * 카테고리 Entity를 생성한다.
     * @param idx
     * @param name
     * @param parentCategory
     * @return {@link Category}
     */
    public static Category category(Long idx, String name, Category parentCategory) {
        Category entity = Category.builder()
            .name(name)
            .parentCategory(parentCategory)
            .build();
        if (idx != null) ReflectionTestUtils.setField(entity, "idx", idx);
        return entity;
    }

    /**
     * 포스트 Entity를 생성한다.
     * @param idx
     * @param category
     * @param title
     * @param content
     * @param desc
     * @param visible
     * @return {@link Post}
     */
    public static Post post(Long idx, Category category, String title, String content, String desc, Boolean visible) {
        Post entity = Post.builder()
            .category(category)
            .title(title)
            .content(content)
            .desc(desc)
            .visible(visible)
            .build();
        if (idx != null) ReflectionTestUtils.setField(entity, "idx", idx);
        return entity;
    }
}
