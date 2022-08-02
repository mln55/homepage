package com.personalproject.homepage.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalproject.homepage.config.CustomUnitTestSecurityConfig;
import com.personalproject.homepage.config.web.PageType;
import com.personalproject.homepage.config.web.ViewName;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.dto.PostsPaginationDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.EntityCreator;
import com.personalproject.homepage.service.CategoryService;
import com.personalproject.homepage.service.PostService;

@WebMvcTest(PageViewController.class)
@CustomUnitTestSecurityConfig
@ActiveProfiles("test")
public class PageViewControllerTest {

    private static final String HTML_CONTENT_TYPE = "text/html; charset=utf8";
    private static final Boolean TEST_VISIBLE = true;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // service에서 return받은 entity
    private static final Category testParentCategory = EntityCreator.category(99l, "parent", null);
    private static final Category testChildCategory = EntityCreator.category(100l, "child", testParentCategory);
    private static final Category testChildCategory2 = EntityCreator.category(101l, "child2", testParentCategory);

    private static final Post testPost = EntityCreator.post(99l, testChildCategory, "title", "content", "desc", TEST_VISIBLE);
    private static final List<Post> testPostList = LongStream.rangeClosed(1, 7)
        .mapToObj(id -> EntityCreator.post(id, id <= 4 ? testChildCategory : testChildCategory2, "title", "content", "desc", TEST_VISIBLE))
        .collect(Collectors.toList());

    private static final List<Post> testChildCategoryPostList = new ArrayList<>(testPostList.subList(0, 5));
    private static final List<Category.WithPostsCount> testPostsCountList = List.of(
        new Category.WithPostsCount(testParentCategory, 0l),
        new Category.WithPostsCount(testChildCategory, 4l),
        new Category.WithPostsCount(testChildCategory2, 3l)
    );

    // client로 보낼 dto 및 object
    private static final CategoryDto.Res testParentCategoryResult = CategoryDto.Res.builder().name("parent").build();
    private static final CategoryDto.Res testChildCategoryResult = CategoryDto.Res.builder().name("child").parent("parent").build();
    private static final CategoryDto.Res testChildCategory2Result = CategoryDto.Res.builder().name("child2").parent("parent").build();

    private static final PostDto.Res testPostResult = PostDto.Res.builder()
        .id(99l)
        .category(testChildCategoryResult)
        .title("title")
        .content("content")
        .desc("desc")
        .visible(TEST_VISIBLE)
        .hit(0l)
        .build();
    private static final List<PostDto.Res> testPostListResult = LongStream.rangeClosed(1, 7)
        .mapToObj(id -> PostDto.Res.builder()
            .id(id)
            .category(id <= 4 ? testChildCategoryResult : testChildCategory2Result)
            .title("title")
            .content("content")
            .desc("desc")
            .visible(TEST_VISIBLE)
            .hit(0l)
            .build())
        .collect(Collectors.toList());

    private static final List<PostDto.Res> testChildCategoryPostListResult = new ArrayList<>(testPostListResult.subList(0, 5));
    private static final List<CategoryDto.ResWithPostsCount> testPostsCountListResult = new ArrayList<>();
    private static final long totalPostsCount = 7l;
    private static final PostsPaginationDto testPostsPagination = new PostsPaginationDto(1, 1);

    @BeforeAll
    static void setPostsCountList() {
        CategoryDto.ResWithPostsCount pc = CategoryDto.ResWithPostsCount.builder()
            .categoryId(testParentCategory.getIdx())
            .name(testParentCategory.getName())
            .postsCount(7l)
            .build();
        CategoryDto.ResWithPostsCount cpc = CategoryDto.ResWithPostsCount.builder()
            .categoryId(testChildCategory.getIdx())
            .name(testChildCategory.getName())
            .postsCount(4l)
            .parentId(testParentCategory.getIdx())
            .build();
        CategoryDto.ResWithPostsCount cpc2 = CategoryDto.ResWithPostsCount.builder()
            .categoryId(testChildCategory2.getIdx())
            .name(testChildCategory2.getName())
            .postsCount(3l)
            .parentId(testParentCategory.getIdx())
            .build();
        pc.addChild(cpc);
        pc.addChild(cpc2);

        testPostsCountListResult.add(pc);
    }

    @Autowired MockMvc mockMvc;

    @MockBean PostService postService;

    @MockBean CategoryService categoryService;

    @Nested
    @DisplayName("GET / and /category - indexPage")
    class Test_Get_IndexPage {
        @Test
        @DisplayName("성공: '/' 인덱스 페이지 요청")
        void Success_IndexPage_ReturnModelAndView_ROOT() throws Exception {
            successIndexPageReturnModelAndView("/");
        }
        @Test
        @DisplayName("실패: '/' 포스트가 없는 페이지 쿼리 - 404 에러 페이지")
        void Fail_PageQueryOutOfBounds_Forward404Page_ROOT() throws Exception {
            failPageQueryOutOfBoundsForward404Page("/");
        }

        @Test
        @DisplayName("실패: '/' 유효하지 않은 페이지 쿼리 - 500 에러 페이지")
        void Fail_InvalidPageQuery_Forward404Page_ROOT() throws Exception {
            failInvalidPageQueryForward404Page("/");
        }

        @Test
        @DisplayName("성공: '/category' 인덱스 페이지 요청")
        void Success_IndexPage_ReturnModelAndView_CATEGORY() throws Exception {
            successIndexPageReturnModelAndView("/category");
        }

        @Test
        @DisplayName("실패: '/category' 포스트가 없는 페이지 쿼리 - forward 404 에러 페이지")
        void Fail_PageQueryOutOfBounds_Forward404Page_CATEGORY() throws Exception {
            failPageQueryOutOfBoundsForward404Page("/category");
        }

        @Test
        @DisplayName("실패: '/category' 유효하지 않은 페이지 쿼리 - forward 500 에러 페이지")
        void Fail_InvalidPageQuery_Forward404Page_CATEGORY() throws Exception {
            failInvalidPageQueryForward404Page("/category");
        }

        // 인덱스 페이지 요청 성공
        void successIndexPageReturnModelAndView(final String uri) throws Exception {
            given(postService.getPostsByVisible(eq(TEST_VISIBLE), any(Pageable.class))).willReturn(testPostList);
            given(categoryService.getAllCategoriesWithPostsCount(eq(TEST_VISIBLE))).willReturn(testPostsCountList);

            // when
            ResultActions result = mockMvc.perform(get(uri));

            // then
            verify(postService).getPostsByVisible(eq(TEST_VISIBLE), any(Pageable.class));
            verify(categoryService).getAllCategoriesWithPostsCount(eq(TEST_VISIBLE));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageIndex"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.INDEX),
                model().attributeDoesNotExist("selectedCategory"),
                model().attribute("postList", samePropertyValuesAs(testPostListResult)),
                model().attribute("totalPostsCount", equalTo(totalPostsCount)),
                model().attribute("postsCountList", samePropertyValuesAs(testPostsCountListResult)),
                model().attribute("pagination", samePropertyValuesAs(testPostsPagination)),
                model().attribute("pageType", equalTo(PageType.POST_LIST))
            ));
        }

        // 인덱스 페이지 실패 - 포스트가 없는 페이지 쿼리
        void failPageQueryOutOfBoundsForward404Page(final String uri) throws Exception {
            // given
            given(postService.getPostsByVisible(eq(TEST_VISIBLE), any(Pageable.class))).willReturn(new ArrayList<>());

            // when
            ResultActions result = mockMvc.perform(get(uri)
                .queryParam("page", "99999") // page has no posts
            );

            // then
            verify(postService).getPostsByVisible(eq(TEST_VISIBLE), any(Pageable.class));
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageIndex"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        // 인덱스 페이지 실패 - 유효하지 않은 페이지 쿼리
        void failInvalidPageQueryForward404Page(final String uri) throws Exception {
            // given
            // thrown at SimplePageableHandlerMethodArgumentResolver

            // when
            ResultActions result = mockMvc.perform(get(uri)
                .queryParam("page", "NaN or Not parsable to Integer")
            );

            // then
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageIndex"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }
    }

    @Nested
    @DisplayName("GET /{postId}")
    class Test_Get_PostPage {
        @Test
        @DisplayName("성공: postId에 맞는 포스트 페이지 요청")
        void Success_PostPage_ReturnModelAndView() throws Exception {
            // given
            given(postService.getPost(anyLong())).willReturn(testPost);
            given(categoryService.getAllCategoriesWithPostsCount(eq(TEST_VISIBLE))).willReturn(testPostsCountList);

            // when
            ResultActions result = mockMvc.perform(get("/" + testPost.getIdx()));

            // then
            verify(postService).getPost(anyLong());
            verify(categoryService).getAllCategoriesWithPostsCount(eq(TEST_VISIBLE));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pagePost"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.INDEX),
                model().attribute("post", samePropertyValuesAs(testPostResult, "category")),
                model().attribute("post", hasProperty("category", samePropertyValuesAs(testPostResult.getCategory()))),
                model().attribute("selectedCategory", samePropertyValuesAs(testPostResult.getCategory())),
                model().attribute("totalPostsCount", equalTo(totalPostsCount)),
                model().attribute("postsCountList", samePropertyValuesAs(testPostsCountListResult)),
                model().attribute("pageType", equalTo(PageType.POST_DETAIL))
            ));
        }

        @Test
        @DisplayName("실패: 비공개 포스트 - forward 404 에러 페이지")
        void Fail_InvisiblePost_Forward404Page() throws Exception {
            // given
            Post post = MAPPER.readValue(MAPPER.writeValueAsString(testPost), Post.class);
            ReflectionTestUtils.setField(post, "visible", false);
            given(postService.getPost(anyLong())).willReturn(post);

            // when
            ResultActions result = mockMvc.perform(get("/" + post.getIdx()));

            // then
            verify(postService).getPost(anyLong());
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pagePost"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 포스트 없음 - forward 404 에러 페이지")
        void Fail_NonExistentPost_Forward404Page() throws Exception {
            // given
            given(postService.getPost(anyLong())).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "포스트")
            );

            // when
            ResultActions result = mockMvc.perform(get("/99999")); // not existent

            // then
            verify(postService).getPost(anyLong());
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pagePost"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 올바르지 않은 postId - forward 404 에러 페이지")
        void Fail_InvalidPostId_Forward404Page() throws Exception {
            // given

            // when
            ResultActions result = mockMvc.perform(get("/NaN or Not integer format"));

            // then
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pagePost"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }
    }

    @Nested
    @DisplayName("GET /category/{name}")
    class Test_Get_PostListPageOfTopCategory {
        @Test
        @DisplayName("성공: 상위 카테고리에 속한 게시글 페이지 요청")
        void Success_TopCategoryPage_ReturnModelAndView() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willReturn(testPostList);
            given(categoryService.getAllCategoriesWithPostsCount(eq(TEST_VISIBLE))).willReturn(testPostsCountList);

            // when
            ResultActions result = mockMvc.perform(get("/category/" + testParentCategory.getName()));

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            verify(categoryService).getAllCategoriesWithPostsCount(eq(TEST_VISIBLE));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.INDEX),
                model().attribute("selectedCategory", samePropertyValuesAs(testParentCategoryResult)),
                model().attribute("postList", samePropertyValuesAs(testPostListResult)),
                model().attribute("totalPostsCount", equalTo(totalPostsCount)),
                model().attribute("postsCountList", samePropertyValuesAs(testPostsCountListResult)),
                model().attribute("pagination", samePropertyValuesAs(testPostsPagination)),
                model().attribute("pageType", PageType.POST_LIST)
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 최상위 카테고리 - 404 에러 페이지")
        void Fail_NonExistentTopCategory_Forward404Page() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willThrow(new ApiException(ErrorMessage.NON_EXISTENT, "카테고리"));

            // when
            ResultActions result = mockMvc.perform(get("/category/NonExistent"));

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 범위를 벗어난 페이지 쿼리 - 404 에러 페이지")
        void Fail_PageQueryOutOfBounds_Forward404Page() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willReturn(new ArrayList<>());

            // when
            ResultActions result = mockMvc.perform(get("/category/" + testParentCategory.getName())
                .queryParam("page", "999999999")
            );

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 페이지 쿼리 - 404 에러 페이지")
        void Fail_InvalidPageQuery_Forward404Page() throws Exception {
            // given

            // when
            ResultActions result = mockMvc.perform(get("/category/" + testParentCategory.getName())
                .queryParam("page", "NaN or Not Integer format")
            );

            // then
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }
    }

    @Nested
    @DisplayName("GET /category/{parent}/{name}")
    class Test_Get_PostListPageOfSubCategory {
        @Test
        @DisplayName("성공: 하위 카테고리에 속한 게시글 페이지 요청")
        void Success_SubCategoryPage_ReturnModelAndView() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willReturn(testChildCategoryPostList);
            given(categoryService.getAllCategoriesWithPostsCount(eq(TEST_VISIBLE))).willReturn(testPostsCountList);
            String parent = testParentCategory.getName();
            String child = testChildCategory.getName();

            // when
            ResultActions result = mockMvc.perform(get(String.format("/category/%s/%s", parent, child)));

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            verify(categoryService).getAllCategoriesWithPostsCount(eq(TEST_VISIBLE));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.INDEX),
                model().attribute("selectedCategory", samePropertyValuesAs(testChildCategoryResult)),
                model().attribute("postList", samePropertyValuesAs(testChildCategoryPostListResult)),
                model().attribute("totalPostsCount", equalTo(totalPostsCount)),
                model().attribute("postsCountList", samePropertyValuesAs(testPostsCountListResult)),
                model().attribute("pagination", samePropertyValuesAs(testPostsPagination)),
                model().attribute("pageType", equalTo(PageType.POST_LIST))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 최상위 카테고리 - 404 에러 페이지")
        void Fail_NonExistentTopCategory_Forward404Page() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willThrow(new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리"));

            // when
            ResultActions result = mockMvc.perform(get("/category/NonExistent/" + testChildCategory.getName()));

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 범위를 벗어난 페이지 쿼리 - 404 에러 페이지")
        void Fail_PageQueryOutOfBounds_Forward404Page() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class)))
                .willReturn(new ArrayList<>());

            // when
            ResultActions result = mockMvc.perform(get(String.format("/category/%s/%s", testParentCategory.getName(), testChildCategory.getName()))
                .queryParam("page", "999999999")
            );

            // then
            verify(postService).getPostsByVisibleAndCategory(eq(TEST_VISIBLE), any(CategoryDto.NameReq.class), any(Pageable.class));
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 페이지 쿼리 - 404 에러 페이지")
        void Fail_InvalidPageQuery_Forward404Page() throws Exception {
            // given

            // when
            ResultActions result = mockMvc.perform(get(String.format("/category/%s/%s", testParentCategory.getName(), testChildCategory.getName()))
                .queryParam("page", "NaN or Not Integer format")
            );

            // then
            result.andExpect(matchAll(
                status().isNotFound(),
                handler().handlerType(PageViewController.class),
                handler().methodName("pageCategory"),
                content().contentType(HTML_CONTENT_TYPE),
                view().name(ViewName.ERROR_404)
            ));
        }
    }
}
