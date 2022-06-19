package com.personalproject.homepage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.personalproject.homepage.config.web.ViewName;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.dto.PostsCountByCategoryDto;
import com.personalproject.homepage.error.PageNotFoundException;
import com.personalproject.homepage.model.PostsCountModel;
import com.personalproject.homepage.model.PostsCountModel.SubPostsCountModel;
import com.personalproject.homepage.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class PageViewController {

    private static final boolean VISIBLE = true;

    private final PostService postService;

    @GetMapping({"/", "/category"})
    public ModelAndView pageIndex(Pageable pageable) {
        ModelAndView mv = new ModelAndView(ViewName.CATEGORY);
        List<PostDto> postList = postService.getPostsByVisible(VISIBLE, pageable);

        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        List<PostsCountModel> postsCountList = getPostsCountList();
        long totalPostsCount = postsCountList.stream().mapToLong(pc -> pc.getCount()).sum();
        mv.addObject("selectedCategory", null);
        mv.addObject("postList", postList);
        mv.addObject("totalPostsCount", totalPostsCount);
        mv.addObject("postsCountList", postsCountList);
        return mv;
    }

    @GetMapping("/{postId}")
    public ModelAndView pagePost(@PathVariable String postId) {

        if (!postId.matches("\\d+")) {
            log.info("형식에 맞지 않는 postId 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        ModelAndView mv = new ModelAndView(ViewName.POST);
        PostDto post = postService.getPost(Long.parseLong(postId));
        if (!post.getVisible()) {
            log.info("비공개 포스트 요청. postId: '{}'", postId);
            throw new PageNotFoundException();
        }

        mv.addObject("post", post);
        return mv;
    }

    @GetMapping({"/category/{name}", "/category/{parent}/{name}"})
    public ModelAndView pageCategory(
        Pageable pageable,
        @PathVariable(required = false) String parent,
        @PathVariable() String name
    ) {
        ModelAndView mv = new ModelAndView(ViewName.CATEGORY);
        CategoryDto category = CategoryDto.builder()
            .name(name)
            .parent(parent)
            .build();
        List<PostDto> postList = postService.getPostsByVisibleAndCategory(VISIBLE , category, pageable);

        if (postList.isEmpty() && pageable.getPageNumber() != 0) {
            log.info("포스트가 없는 페이지 요청. page: '{}'", pageable.getPageNumber());
            throw new PageNotFoundException();
        }

        mv.addObject("postList", postList);
        List<PostsCountModel> postsCountList = getPostsCountList();
        long totalPostsCount = postsCountList.stream().mapToLong(pc -> pc.getCount()).sum();

        mv.addObject("totalPostsCount", totalPostsCount);
        mv.addObject("selectedCategory", category);
        mv.addObject("postsCountList", postsCountList);
        return mv;
    }

    private List<PostsCountModel> getPostsCountList() {
        List<PostsCountByCategoryDto> postsCountSource = postService.getPostsCountByVisiblePerCategory(VISIBLE);
        ArrayList<PostsCountModel> postsCountList = new ArrayList<>();
        String parentPrev = null;
        int index = -1;
        int size = 0;
        for (PostsCountByCategoryDto pcSource : postsCountSource) {
            String parent = pcSource.getCategory().getParent();

            // 기존 parent와 source의 parent가 다르면
            if (!parent.equals(parentPrev)) {

                // 리스트에서 source의 parent 인덱스를 찾는다.
                parentPrev = parent;
                index = -1;
                for (int i = 0; i < size; ++i) {
                    if (postsCountList.get(i).getParentName().equals(parent)) {
                        index = i;
                        break;
                    }
                }

                // source의 parent가 없으면 새로 추가한다.
                if (index == -1) {
                    postsCountList.add(new PostsCountModel(parent));
                    index = size;
                    ++size;
                }
            }

            // 정보를 추가한다.
            PostsCountModel pc = postsCountList.get(index);
            pc.getSubPostsCountList().add(new SubPostsCountModel(pcSource.getCategory().getName(), pcSource.getCount()));
            pc.setCount(pc.getCount() + pcSource.getCount());
        }

        Collections.sort(postsCountList);
        postsCountList.forEach(pc -> Collections.sort(pc.getSubPostsCountList()));
        return postsCountList;
    }
}
