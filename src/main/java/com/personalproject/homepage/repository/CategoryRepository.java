package com.personalproject.homepage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.personalproject.homepage.entity.Category;

public interface CategoryRepository extends CommonRepository<Category, Long> {

    Optional<Category> findByNameAndParentCategory(String name, Category parentCategory);

    boolean existsByNameAndParentCategory(String name, Category parentCategory);

    /**
     * 카테고리 정보와 등록된 포스트 수를 {@code visible}에 따라 조회한다.
     * @param visible 포스트 공개 여부, null이면 전부
     * @return {@link Category.WithPostsCount}
     */
    @Query(
        "SELECT new com.personalproject.homepage.entity.Category$WithPostsCount(" +
            "c, SUM(CASE WHEN (:pVisible IS NULL OR p.visible = :pVisible) AND p IS NOT NULL THEN 1 ELSE 0 END)" +
        ")" +
        " FROM Category c" +
        " LEFT JOIN Post p" +
        " ON c = p.category" +
        " GROUP BY c"
    )
    List<Category.WithPostsCount> allCategoriesWithPostsCount(@Param("pVisible") Boolean visible);
}
