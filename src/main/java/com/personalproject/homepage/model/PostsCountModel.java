package com.personalproject.homepage.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostsCountByCategoryDto;
import com.personalproject.homepage.error.ErrorMessage;

import lombok.Getter;
/**********************************************************************
    카테고리 별 포스트 열람 가능 한 포스트 개수를 담을 객체
    페이지 요청 시 포스트의 visible에 따라 카테고리 별 포스트 수를 저장한다.
    view template에서 반복문을 돌며 접근할 수 있다.
**********************************************************************/
@Getter
public class PostsCountModel {

    private String parent;

    private String name;

    private Long count;

    private Long visibleCount;

    private Long invisibleCount;

    private List<PostsCountModel> childList;

    public PostsCountModel(PostsCountByCategoryDto postsCount) {
        parent = postsCount.getCategory().getParent();
        name = postsCount.getCategory().getName();
        visibleCount = postsCount.getVisibleCount();
        invisibleCount = postsCount.getInvisibleCount();
        count = visibleCount + invisibleCount;
    }

    public PostsCountModel(CategoryDto category, Long visibleCount, Long invisibleCount) {
        parent = category.getParent();
        name = category.getName();
        this.visibleCount = visibleCount;
        this.invisibleCount = invisibleCount;
        this.count = visibleCount + invisibleCount;
    }

    public PostsCountModel(String parent, String name, Long visibleCount, Long invisibleCount) {
        this.parent = parent;
        this.name = name;
        this.visibleCount = visibleCount;
        this.invisibleCount = invisibleCount;
        this.count = visibleCount + invisibleCount;
    }

    public void addChild(PostsCountModel child) {
        checkArgument(child != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("child"));
        checkArgument(parent == null, "카테고리는 2차 카테고리까지만 생성할 수 있습니다.");
        if (childList == null) childList = new ArrayList<>();
        childList.add(child);
        visibleCount += child.getVisibleCount();
        invisibleCount += child.getInvisibleCount();
        count += child.getVisibleCount() + child.getInvisibleCount();
    }
}
