package com.personalproject.homepage.repository;

import java.util.List;
import java.util.Optional;

import com.personalproject.homepage.entity.Category;

public interface CategoryRepository extends CommonRepository<Category, Long> {

    Optional<Category> findByNameAndParentCategory(String name, Category parentCategory);

    List<Category> findAllByParentCategory(Category parentCategory);

    List<Category> findAllByParentCategoryIsNull();

    boolean existsByNameAndParentCategory(String name, Category parentCategory);
}
