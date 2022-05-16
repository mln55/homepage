package com.personalproject.homepage.repository;

import java.util.List;
import java.util.Optional;

import com.personalproject.homepage.entity.Category;

public interface CategoryRepository extends CommonRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findAllByParentCategory(Category parentCategory);

    List<Category> findAllByParentCategoryIsNull();

    boolean existsByName(String name);

}
