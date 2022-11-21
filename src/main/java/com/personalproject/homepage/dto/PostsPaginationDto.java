package com.personalproject.homepage.dto;

import lombok.Getter;

/** 선택된 카테고리의 포스트에 대한 페이지 정보를 담은 객체 */
@Getter
public class PostsPaginationDto {

    /** 한 화면에 표시될 페이지 수 */
    private final int interval = 5;

    /** 현재 선택된 페이지 */
    private final int currentPage;

    /** 마지막 페이지 */
    private final int lastPage;

    /** 표시될 첫 번째 페이지 */
    private final int startPage;

    /** 표시될 마지막 페이지 */
    private final int endPage;

    public PostsPaginationDto(int currentPage, int lastPage) {
        this.currentPage = currentPage;
        this.lastPage = lastPage;
        this.startPage = (currentPage - 1) / interval * interval + 1;
        this.endPage = Math.min(lastPage, (currentPage - 1) / interval * interval + interval);
    }
}
