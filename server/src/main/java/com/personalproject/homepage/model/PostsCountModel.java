package com.personalproject.homepage.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**********************************************************************
    카테고리 별 포스트 열람 가능 한 포스트 개수를 담을 객체
    페이지 요청 시 포스트의 visible에 따라 카테고리 별 포스트 수를 저장한다.
    model에 (parentName, name) 오름차순 List로 담기므로
    view template에서 반복문을 돌며 접근할 수 있다.
**********************************************************************/
@Getter
@Setter
public class PostsCountModel implements Comparable<PostsCountModel> {

    private String parentName;
    private Long count;
    private List<SubPostsCountModel> subPostsCountList;

    public PostsCountModel(String parentCategoryName) {
        this.parentName = parentCategoryName;
        this.count = 0l;
        this.subPostsCountList = new ArrayList<>();
    }

    @Override
    public int compareTo(PostsCountModel o) {
        return this.parentName.compareToIgnoreCase(o.parentName);
    }

    @Getter
    public static class SubPostsCountModel implements Comparable<SubPostsCountModel> {
        private String name;
        private Long count;

        public SubPostsCountModel(String categoryName, Long count) {
            this.name = categoryName;
            this.count = count;
        }

        @Override
        public int compareTo(SubPostsCountModel o) {
            return this.name.compareToIgnoreCase(o.name);
        }
    }
}
