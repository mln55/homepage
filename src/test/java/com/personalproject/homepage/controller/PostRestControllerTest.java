package com.personalproject.homepage.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalproject.homepage.config.CustomUnitTestSecurityConfig;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.service.PostService;

@WebMvcTest(PostRestController.class)
@CustomUnitTestSecurityConfig
@WithMockUser(roles = "ADMIN")
@ActiveProfiles("test")
public class PostRestControllerTest {

    private static final String ROOT = "/api/posts";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf8";
    private static final String QUERY_STRING_CNAME = "cName";
    private static final String QUERY_STRING_CPARENT = "cParent";
    private static final String QUERY_STRING_VISIBLE = "visible";

    private static final int TEST_SIZE = 8;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CategoryDto testCategory = CategoryDto.builder().name("name").parent("parent").build();
    private static final List<PostDto> testPostList = LongStream.rangeClosed(1, TEST_SIZE)
        .mapToObj(id -> PostDto.builder().id(id).category(testCategory).visible(id % 2 == 0).build())
        .collect(Collectors.toList());
    private static final List<PostDto> testVisiblePostList = LongStream.rangeClosed(1, TEST_SIZE)
        .mapToObj(id -> PostDto.builder().id(id).category(testCategory).visible(true).build())
        .collect(Collectors.toList());
    private static final List<PostDto> testInvisiblePostList = LongStream.rangeClosed(1, TEST_SIZE)
        .mapToObj(id -> PostDto.builder().id(id).category(testCategory).visible(false).build())
        .collect(Collectors.toList());

    @Autowired MockMvc mockMvc;

    @MockBean PostService postService;

    @Nested
    @DisplayName("GET /api/posts")
    class Test_Get_Posts {
        @Test
        @DisplayName("성공: 페이지에 맞는 포스트를 반환한다.")
        void Success_GetPosts_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPosts(any(Pageable.class))).willReturn(testPostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT));

            // then
            verify(postService).getPosts(any(Pageable.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == ture인 포스트를 반환한다.")
        void Success_GetVisiblePosts_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByVisible(
                anyBoolean(), any(Pageable.class)
            )).willReturn(testVisiblePostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_VISIBLE, "true")
            );

            // then
            verify(postService).getPostsByVisible(anyBoolean(), any(Pageable.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty())),
                jsonPath("$.response[?(@.visible != true)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == false인 포스트를 반환한다.")
        void Success_GetInvisiblePosts_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByVisible(
                anyBoolean(), any(Pageable.class)
            )).willReturn(testInvisiblePostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_VISIBLE, "false")
            );

            // then
            verify(postService).getPostsByVisible(anyBoolean(), any(Pageable.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty())),
                jsonPath("$.response[?(@.visible != false)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 페이지, 카테고리에 맞는 포스트를 반환한다.")
        void Success_GetPostsByCategory_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByCategory(
                any(CategoryDto.class), any(Pageable.class)
            )).willReturn(testPostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CNAME, testCategory.getName())
                .param(QUERY_STRING_CPARENT, testCategory.getParent())
            );

            // then
            verify(postService).getPostsByCategory(any(CategoryDto.class), any(Pageable.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty())),
                jsonPath("$.response[?(@.category.name == null)]", is(empty())),
                jsonPath("$.response[?(@.category.parent == null)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 페이지, 카테고리에 맞는 visible == true인 포스트를 반환한다.")
        void Success_GetVisiblePostsByCategory_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(
                anyBoolean(), any(CategoryDto.class), any(Pageable.class)
            )).willReturn(testVisiblePostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CNAME, testCategory.getName())
                .param(QUERY_STRING_CPARENT, testCategory.getParent())
                .param(QUERY_STRING_VISIBLE, "true")
            );

            // then
            verify(postService).getPostsByVisibleAndCategory(
                anyBoolean(), any(CategoryDto.class), any(Pageable.class)
            );
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty())),
                jsonPath("$.response[?(@.category.name == null)]", is(empty())),
                jsonPath("$.response[?(@.category.parent == null)]", is(empty())),
                jsonPath("$.response[?(@.visible != true)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 페이지, 카테고리에 맞는 visible == false인 포스트를 반환한다.")
        void Success_GetInvisiblePostsByCategory_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByVisibleAndCategory(
                anyBoolean(), any(CategoryDto.class), any(Pageable.class)
            )).willReturn(testInvisiblePostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CNAME, testCategory.getName())
                .param(QUERY_STRING_CPARENT, testCategory.getParent())
                .param(QUERY_STRING_VISIBLE, "false")
            );

            // then
            verify(postService).getPostsByVisibleAndCategory(
                anyBoolean(), any(CategoryDto.class), any(Pageable.class))
            ;
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", lessThanOrEqualTo(TEST_SIZE)),
                jsonPath("$.response[?(@.id == null)]", is(empty())),
                jsonPath("$.response[?(@.category.name == null)]", is(empty())),
                jsonPath("$.response[?(@.category.parent == null)]", is(empty())),
                jsonPath("$.response[?(@.visible != false)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 query string - visible")
        void Fail_InvalidQueryStringVisible_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.INVALID_QUERY_STRING;
            String message = errorMessage.getMessage("visible", "true, false");
            int status = errorMessage.getStatus().value();
            String visible = "invalid";

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_VISIBLE, visible)
            );

            // then
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPosts"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }
    }

    @Nested
    @DisplayName("POST /api/posts")
    class Test_Post_Post {
        @Test
        @DisplayName("성공: 포스트를 추가한다.")
        void Success_NewPost_ReturnApiResultOfDto() throws Exception {
            // given
            PostDto post = PostDto.builder()
                .id(1L)
                .category(testCategory)
                .title("title")
                .content("content")
                .visible(true)
                .build();
            given(postService.createPost(any(PostDto.class))).willReturn(post);

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post))
            );

            // then
            verify(postService).createPost(any(PostDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("createPost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.id", is((int) post.getId().longValue())),
                jsonPath("$.response.category.name", is(post.getCategory().getName())),
                jsonPath("$.response.category.parent", is(post.getCategory().getParent())),
                jsonPath("$.response.title", is(post.getTitle())),
                jsonPath("$.response.content", is(post.getContent())),
                jsonPath("$.response.visible", is(post.getVisible()))
            ));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id}")
    class Test_Get_Post {
        @Test
        @DisplayName("성공: 아이디에 맞는 포스트를 반환한다.")
        void Success_GetPost_ReturnApiResultOfDto() throws Exception {
            // given
            PostDto post = PostDto.builder()
                .id(1L)
                .category(testCategory)
                .title("title")
                .content("content")
                .visible(true)
                .build();
            given(postService.getPost(anyLong())).willReturn(post);

            // when
            ResultActions result = mockMvc.perform(get(ROOT + "/1"));

            // then
            verify(postService).getPost(anyLong());
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.id", is((int) post.getId().longValue())),
                jsonPath("$.response.category.name", is(post.getCategory().getName())),
                jsonPath("$.response.category.parent", is(post.getCategory().getParent())),
                jsonPath("$.response.title", is(post.getTitle())),
                jsonPath("$.response.content", is(post.getContent())),
                jsonPath("$.response.visible", is(post.getVisible()))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트")
        void Fail_NonExistentPost_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("포스트");
            int status = errorMessage.getStatus().value();
            given(postService.getPost(anyLong())).willThrow(
                new ApiException(errorMessage, "포스트")
            );

            // when
            ResultActions result = mockMvc.perform(get(ROOT + "/1"));

            // then
            verify(postService).getPost(anyLong());
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 pathvariable - id")
        void Fail_InvalidPathVariableId_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.INVALID_PATH_PARAM;
            String message = errorMessage.getMessage("id");
            int status = errorMessage.getStatus().value();

            // when
            ResultActions result = mockMvc.perform(get(ROOT + "/NotANumber"));

            // then
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(PostRestController.class),
                handler().methodName("getPost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/posts/{id}")
    class Test_Patch_Post {
        @Test
        @DisplayName("성공: 포스트 수정 후 dto를 반환한다.")
        void Success_PatchPost_ReturnApiResultOfDto() throws Exception {
            // given
            PostDto post = PostDto.builder()
                .id(1L)
                .category(testCategory)
                .title("title")
                .content("content")
                .visible(true)
                .build();
            PostDto inputPost = PostDto.builder().category(testCategory).build();
            given(postService.updatePost(anyLong(), any(PostDto.class))).willReturn(post);

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPost))
            );

            // when
            verify(postService).updatePost(anyLong(), any(PostDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("updatePost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.id", is((int) post.getId().longValue())),
                jsonPath("$.response.category", is(notNullValue())),
                jsonPath("$.response.category.name", is(post.getCategory().getName())),
                jsonPath("$.response.category.parent", is(post.getCategory().getParent())),
                jsonPath("$.response.title", is(post.getTitle())),
                jsonPath("$.response.content", is(post.getContent())),
                jsonPath("$.response.visible", is(post.getVisible()))
            ));
        }

        @Test
        @DisplayName("실패: 변경 사항이 없다.")
        void Fail_NonExistentPost_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NO_CHANGES;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(postService.updatePost(anyLong(), any(PostDto.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PostDto.builder().build()))
            );

            // then
            verify(postService).updatePost(anyLong(), any(PostDto.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(PostRestController.class),
                handler().methodName("updatePost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{id}")
    class Test_Delete_Post {
        @Test
        @DisplayName("성공: 포스트 삭제")
        void Success_DeletePost_ReturnApiResultOfTrue() throws Exception {
            // given
            given(postService.deletePost(anyLong())).willReturn(true);

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/1"));

            // then
            verify(postService).deletePost(anyLong());
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("deletePost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response", is(true))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 삭제")
        void Fail_NonExistentPost_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("포스트");
            int status = errorMessage.getStatus().value();
            given(postService.deletePost(anyLong())).willThrow(
                new ApiException(errorMessage, "포스트")
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/1"));

            // then
            verify(postService).deletePost(anyLong());
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(PostRestController.class),
                handler().methodName("deletePost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }
    }
}
