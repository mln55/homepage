package com.personalproject.homepage.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.personalproject.homepage.config.web.PageType;
import com.personalproject.homepage.config.web.ViewName;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.dto.PostsCountByCategoryDto;
import com.personalproject.homepage.dto.PostsPaginationDto;
import com.personalproject.homepage.error.PageNotFoundException;
import com.personalproject.homepage.model.PostsCountModel;
import com.personalproject.homepage.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class PageViewController {

    private static final boolean VISIBLE = true;

    private static final int PAGE_SIZE = 8;

    private static final String KEY_TPC = "totalPostsCount";

    private static final String KEY_VPC = "visiblePostsCount";

    private static final String KEY_IPC = "invisiblePostsCount";

    private final PostService postService;

    @GetMapping({"/", "/category"})
    public ModelAndView pageIndex(Pageable pageable) {
        ModelAndView mv = new ModelAndView(ViewName.INDEX);
        List<PostDto> postList = postService.getPostsByVisible(VISIBLE, pageable);

        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        List<PostsCountModel> postsCountList = getPostsCountList();
        Map<String, Long> countMap = getCountMap(postsCountList);
        PostsPaginationDto postsPagination = new PostsPaginationDto(
            pageable.getPageNumber() + 1,
            (countMap.get(KEY_VPC).intValue() - 1) / PAGE_SIZE + 1
        );
        mv.addObject("selectedCategory", null);
        mv.addObject("postList", postList);
        mv.addObject("postsCountList", postsCountList);
        mv.addObject("pagination", postsPagination);
        mv.addObject("pageType", PageType.POST_LIST);
        mv.addAllObjects(countMap);
        return mv;
    }

    @GetMapping("/{postId}")
    public ModelAndView pagePost(
        @PathVariable String postId,
        HttpServletRequest request
    ) {

        if (!postId.matches("\\d+")) {
            log.info("형식에 맞지 않는 postId 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        ModelAndView mv = new ModelAndView(ViewName.INDEX);
        PostDto post = postService.getPost(Long.parseLong(postId));
        if (!post.getVisible() && !request.isUserInRole("ADMIN")) {
            log.info("비공개 포스트 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        List<PostsCountModel> postsCountList = getPostsCountList();

        mv.addObject("post", post);
        mv.addObject("selectedCategory", post.getCategory());
        mv.addObject("postsCountList", postsCountList);
        mv.addAllObjects(getCountMap(postsCountList));
        mv.addObject("pageType", PageType.POST_DETAIL);
        return mv;
    }

    @GetMapping({"/category/{name}", "/category/{parent}/{name}"})
    public ModelAndView pageCategory(
        Pageable pageable,
        @PathVariable(required = false) String parent,
        @PathVariable String name
    ) {
        ModelAndView mv = new ModelAndView(ViewName.INDEX);
        CategoryDto category = CategoryDto.builder()
            .name(name)
            .parent(parent)
            .build();
        List<PostDto> postList = postService.getPostsByVisibleAndCategory(VISIBLE , category, pageable);

        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        List<PostsCountModel> postsCountList = getPostsCountList();

        String categoryName = category.getParent() == null ? category.getName() : category.getParent();
        PostsPaginationDto postsPagination = new PostsPaginationDto(1, 1);
        for (PostsCountModel pc : postsCountList) {
            if (categoryName.equals(pc.getParent())) {
                postsPagination = new PostsPaginationDto(pageable.getPageNumber() + 1, (pc.getVisibleCount().intValue() - 1) / PAGE_SIZE + 1);
                break;
            }
        }

        mv.addObject("selectedCategory", category);
        mv.addObject("postList", postList);
        mv.addObject("postsCountList", postsCountList);
        mv.addObject("pagination", postsPagination);
        mv.addAllObjects(getCountMap(postsCountList));
        mv.addObject("pageType", PageType.POST_LIST);
        return mv;
    }

    private List<PostsCountModel> getPostsCountList() {
        List<PostsCountByCategoryDto> postsCountSource = postService.getPostsCountPerCategory();

        postsCountSource.sort((o1, o2) -> {
            CategoryDto c1 = o1.getCategory();
            CategoryDto c2 = o2.getCategory();
            String p1 = c1.getParent();
            String p2 = c2.getParent();
            String n1 = c1.getName();
            String n2 = c2.getName();

            // super name
            if (p1 == null && p2 == null) {
                return n1.compareTo(n2);
            // sub name
            } else if (p1 != null && p2 != null) {
                return p1.equals(p2) ? n1.compareTo(n2) : p1.compareTo(p2);
            // sup name vs sub parent
            } else {
                return p1 == null ? n1.compareTo(p2) : p1.compareTo(n2);
            }
        });

        ArrayList<PostsCountModel> postsCountList = new ArrayList<>();
        PostsCountModel currentParent = null;
        for (PostsCountByCategoryDto pcSource : postsCountSource) {
            if (currentParent == null || !currentParent.getName().equals(pcSource.getCategory().getParent())) {
                currentParent = new PostsCountModel(null, pcSource.getCategory().getName(), 0L, 0L);
                postsCountList.add(currentParent);
            } else {
                currentParent.addChild(new PostsCountModel(pcSource));
            }
        }
        return postsCountList;
    }

    private Map<String, Long> getCountMap(List<PostsCountModel> postsCountList) {
        Map<String, Long> countMap = new HashMap<>();
        countMap.put(KEY_TPC, 0l);
        countMap.put(KEY_VPC, 0l);
        countMap.put(KEY_IPC, 0l);
        for (PostsCountModel pc : postsCountList) {
            countMap.put(KEY_TPC, countMap.get(KEY_TPC) + pc.getCount());
            countMap.put(KEY_VPC, countMap.get(KEY_VPC) + pc.getVisibleCount());
            countMap.put(KEY_IPC, countMap.get(KEY_IPC) + pc.getInvisibleCount());
        }
        return countMap;
    }
}
