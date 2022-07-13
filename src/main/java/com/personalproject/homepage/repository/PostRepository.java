package com.personalproject.homepage.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.entity.groupby.PostsCountByCategory;
import com.personalproject.homepage.entity.jpql.PostsCountGroupByCategory;

public interface PostRepository extends CommonRepository<Post, Long> {

    // TODO - validation for creating post

    List<Post> findAll(Pageable pageable);

    List<Post> findAllByVisible(Boolean visible, Pageable pageable);

    List<Post> findAllByCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleAndCategory(Boolean visible, Category category, Pageable pageable);

    // underscore가 없어도 된다.
    List<Post> findAllByCategory_ParentCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleAndCategory_ParentCategory(Boolean visible, Category category, Pageable pageable);

    @PostsCountGroupByCategory
    List<PostsCountByCategory> countAllGroupByCategory();

}
