package com.personalproject.homepage.repository;

import java.util.List;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;

import org.springframework.data.domain.Pageable;

public interface PostRepository extends CommonRepository<Post, Long> {

    List<Post> findAll(Pageable pageable);

    List<Post> findAllByVisibleTrue(Pageable pageable);

    List<Post> findAllByCategory(Category category, Pageable pageable);

    List<Post> findAllByVisibleTrueAndCategory(Category category, Pageable pageable);
}
