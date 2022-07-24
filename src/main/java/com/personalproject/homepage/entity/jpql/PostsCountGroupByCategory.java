package com.personalproject.homepage.entity.jpql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.jpa.repository.Query;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Query(
    "SELECT new com.personalproject.homepage.entity.groupby.PostsCountByCategory(" +
        "c" +
        ", SUM(CASE WHEN p.visible = true THEN 1 ELSE 0 END)" +
        ", SUM(CASE WHEN p.visible = false THEN 1 ELSE 0 END)" +
    ")" +
    " FROM Category c" +
    " LEFT JOIN Post p" +
    " ON c = p.category" +
    " GROUP BY c"
)
public @interface PostsCountGroupByCategory{ }
