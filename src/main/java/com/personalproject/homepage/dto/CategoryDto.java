package com.personalproject.homepage.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Category entity에 대응하는 DTO 클래스
 */
public class CategoryDto {

    /**
     * 카테고리 요청에 사용되는 DTO 내부 클래스
     */
    @Getter
    public static class Req {
        private String name;
        private Long parentId;
    }

    /**
     * id 없는 카테고리 요청에 사용되는 DTO 내부 클래스
     */
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NameReq {
        private String name;
        private String parent;
    }

    /**
     * 카테고리 응답에 사용되는 DTO 내부 클래스
     */
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Res {
        private String name;
        private String parent;
    }

    /**
     * 카테고리에 등록된 게시글 수를 포함한 응답에 사용되는 DTO 내부 클래스
     */
    @Getter
    public static class ResWithPostsCount {
        private Long categoryId;
        private String name;
        private Long parentId;
        private Long postsCount;
        private List<ResWithPostsCount> childList = new ArrayList<>();

        @Builder
        private ResWithPostsCount(Long categoryId, String name, Long parentId, Long postsCount) {
            this.categoryId = categoryId;
            this.name = name;
            this.parentId = parentId;
            this.postsCount = postsCount;
        }

        /**
         * childList에 child를 추가하고 postsCount를 child의 postsCount만큼 증가 시킨다.
         * @param child {@link CategoryDto.ResWithPostsCount}
         */
        public void addChild(ResWithPostsCount child) {
            this.childList.add(child);
            this.postsCount += child.getPostsCount();
        }
    }
}
