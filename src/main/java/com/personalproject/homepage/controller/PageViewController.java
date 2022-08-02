package com.personalproject.homepage.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import com.personalproject.homepage.dto.PostsPaginationDto;
import com.personalproject.homepage.error.PageNotFoundException;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.mapper.PostMapper;
import com.personalproject.homepage.service.CategoryService;
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

    private final PostService postService;

    private final CategoryService categoryService;

    @GetMapping({"/", "/category"})
    public ModelAndView pageIndex(Pageable pageable) {
        ModelAndView mv = new ModelAndView(ViewName.INDEX);

        // get post dto list
        List<PostDto.Res> postList = postService.getPostsByVisible(VISIBLE, pageable)
            .stream()
            .map(PostMapper::entityToResDto)
            .collect(Collectors.toList());

        // out of bound page request -> 404
        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        // get category dto list
        List<CategoryDto.ResWithPostsCount> postsCountList = CategoryMapper.entityWithPostsCountListToResDtoWithPostsCountList(
            categoryService.getAllCategoriesWithPostsCount(VISIBLE)
        );

        // pagination
        Long totalPostsCount = postsCountList.stream().mapToLong(pc -> pc.getPostsCount()).sum();
        PostsPaginationDto postsPagination = new PostsPaginationDto(
            pageable.getPageNumber() + 1,
            (totalPostsCount.intValue() - 1) / PAGE_SIZE + 1
        );

        mv.addObject("selectedCategory", null);
        mv.addObject("postList", postList);
        mv.addObject("postsCountList", postsCountList);
        mv.addObject("totalPostsCount", totalPostsCount);
        mv.addObject("pagination", postsPagination);
        mv.addObject("pageType", PageType.POST_LIST);
        return mv;
    }

    @GetMapping("/{postId}")
    public ModelAndView pagePost(
        @PathVariable String postId,
        HttpServletRequest request
    ) {

        // check id format
        if (!postId.matches("\\d+")) {
            log.info("형식에 맞지 않는 postId 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        ModelAndView mv = new ModelAndView(ViewName.INDEX);

        // get post dto
        PostDto.Res post = PostMapper.entityToResDto(postService.getPost(Long.parseLong(postId)));

        // invisible post -> 404
        if (!post.getVisible() && !request.isUserInRole("ADMIN")) {
            log.info("비공개 포스트 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        // get category dto list
        List<CategoryDto.ResWithPostsCount> postsCountList = CategoryMapper.entityWithPostsCountListToResDtoWithPostsCountList(
            categoryService.getAllCategoriesWithPostsCount(VISIBLE)
        );
        Long totalPostsCount = postsCountList.stream().mapToLong(pc -> pc.getPostsCount()).sum();

        mv.addObject("post", post);
        mv.addObject("selectedCategory", post.getCategory());
        mv.addObject("postsCountList", postsCountList);
        mv.addObject("totalPostsCount", totalPostsCount);
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

        // create req dto used by service
        CategoryDto.NameReq categoryDto = CategoryDto.NameReq.builder()
            .name(name)
            .parent(parent)
            .build();

        // get post dto list
        List<PostDto.Res> postList = postService.getPostsByVisibleAndCategory(VISIBLE , categoryDto, pageable)
            .stream()
            .map(PostMapper::entityToResDto)
            .collect(Collectors.toList());

        // out ot bound page request -> 404
        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        // get category dto list
        List<CategoryDto.ResWithPostsCount> postsCountList = CategoryMapper.entityWithPostsCountListToResDtoWithPostsCountList(
            categoryService.getAllCategoriesWithPostsCount(VISIBLE)
        );

        // pagination
        Long totalPostsCount = postsCountList.stream().mapToLong(pc -> pc.getPostsCount()).sum();
        Long paginationPostsCount = 0l;
        for (CategoryDto.ResWithPostsCount pc : postsCountList) {
            // parent == null -> name과 비교
            if (parent == null) {
                if (name.equals(pc.getName())) {
                    paginationPostsCount = pc.getPostsCount();
                    break;
                }

            // parnet != null, parent == name -> parent, name과 비교
            } else if (parent.equals(pc.getName())) {
                // child list loop
                for (CategoryDto.ResWithPostsCount cpc : pc.getChildList()) {
                    if (name.equals(cpc.getName())) {
                        paginationPostsCount = cpc.getPostsCount();
                        break;
                    }
                }
                break;
            }
        }
        PostsPaginationDto postsPagination = new PostsPaginationDto(pageable.getPageNumber() + 1, (paginationPostsCount.intValue() - 1) / PAGE_SIZE + 1);

        mv.addObject("selectedCategory", CategoryDto.Res.builder().name(name).parent(parent).build());
        mv.addObject("postList", postList);
        mv.addObject("postsCountList", postsCountList);
        mv.addObject("pagination", postsPagination);
        mv.addObject("totalPostsCount", totalPostsCount);
        mv.addObject("pageType", PageType.POST_LIST);
        return mv;
    }
}
