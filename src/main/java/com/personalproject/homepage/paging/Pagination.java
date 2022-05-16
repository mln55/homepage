package com.personalproject.homepage.paging;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/********************************************************************************
    JPA repository interface의 페이징 쿼리를 위한 클래스
    {@link org.springframework.data.domain.Pageable}을 반환하는 메소드를 구현한다.
********************************************************************************/
public class Pagination {

    public static final int DEFAULT_PAGE_SIZE = 8; // 기본 페이지 사이즈
    public static final int DEFAULT_OFFSET = 0; // 기본 페이지 사이즈
    public static final String DEFAULT_ORDERBY_COLUMN = "createAt"; // 정렬 기준 컬럼

    public static final Pageable DEFAULT_PAGEREQUEST(Integer page) {

        if (page == null) {
            page = DEFAULT_OFFSET;
        }
        checkArgument(page > 0, "page는 0보다 커야합니다.");
        --page; // using zero-based page index
        return PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(DEFAULT_ORDERBY_COLUMN).descending());
    }
}
