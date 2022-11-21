package com.personalproject.homepage.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;

public interface PostRepository extends CommonRepository<Post, Long> {

    // TODO - validation for creating post

    List<Post> findAll(Pageable pageable);

    List<Post> findAllByVisible(Boolean visible, Pageable pageable);

    List<Post> findAllByCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleAndCategory(Boolean visible, Category category, Pageable pageable);

    /**
     * parameter category 및 하위 category에 속한 포스트를 페이지에 맞게 조회한다.
     * @param category {@link Category}
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Query("SELECT p from Post p WHERE p.category = ?1 OR p.category.parentCategory = ?1")
    List<Post> findAllIncludeChildCategory(Category category, Pageable pageable);

    /**
     * parameter category 및 하위 category에 속한 포스트를 visible, 페이지에 맞게 조회한다.
     * @param visible 공개 여부
     * @param category {@link Category}
     * @param pageable 페이지
     * @return {@link Post} List
     */
    @Query("SELECT p from Post p WHERE p.visible = ?1 AND (p.category = ?2 OR p.category.parentCategory = ?2)")
    List<Post> findAllVisibleIncludeChildCategory(boolean visible, Category category, Pageable pageable);
}
