package com.personalproject.homepage.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.entity.groupby.PostsCountByCategory;
import com.personalproject.homepage.entity.jpql.PostsCountByVisibleGroupByCategory;
import com.personalproject.homepage.entity.jpql.PostsCountGroupByCategory;

public interface PostRepository extends CommonRepository<Post, Long> {

    List<Post> findAll(Pageable pageable);

    List<Post> findAllByVisibleTrue(Pageable pageable);

    List<Post> findAllByVisibleFalse(Pageable pageable);

    List<Post> findAllByCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleTrueAndCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleFalseAndCategory(Category category, Pageable pageable);

    @PostsCountGroupByCategory
    List<PostsCountByCategory> countAllGroupByCategory();

    @PostsCountByVisibleGroupByCategory
    List<PostsCountByCategory> countAllByVisibleGroupByCategory(@Param("visible") Boolean visible);
}
