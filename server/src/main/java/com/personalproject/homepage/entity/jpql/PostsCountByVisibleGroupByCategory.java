package com.personalproject.homepage.entity.jpql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.jpa.repository.Query;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Query(
    "SELECT new com.personalproject.homepage.entity.groupby.PostsCountByCategory(p.category, count(*))" +
    " FROM Post p" +
    " WHERE p.visible = :visible" +
    " GROUP BY p.category"
)
public @interface PostsCountByVisibleGroupByCategory { }
