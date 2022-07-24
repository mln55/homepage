package com.personalproject.homepage.entity.groupby;

import com.personalproject.homepage.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // JPQL로 매핑하기 위해 public 생성자가 필요하다.
public class PostsCountByCategory {

    private Category category;

    private Long visibleCount;

    private Long invisibleCount;
}
