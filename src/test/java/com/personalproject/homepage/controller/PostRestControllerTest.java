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
import static org.mockito.ArgumentMatchers.eq;
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
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.DtoCreator;
import com.personalproject.homepage.helper.EntityCreator;
import com.personalproject.homepage.service.PostService;

@WebMvcTest(PostRestController.class)
@CustomUnitTestSecurityConfig
@WithMockUser(roles = "ADMIN")
@ActiveProfiles("test")
public class PostRestControllerTest {

    private static final String ROOT = "/api/posts";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf8";
    private static final String QUERY_STRING_CATEGORYID = "categoryid";
    private static final String QUERY_STRING_VISIBLE = "visible";

    private static final int TEST_SIZE = 8;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Category testCategory = EntityCreator.category(100l, "name",
        EntityCreator.category(99l, "parent", null)
    );
    private static final List<Post> testPostList = LongStream.rangeClosed(1, TEST_SIZE)
        .mapToObj(id -> EntityCreator.post(id, testCategory, "title", "content", "desc", id % 2 == 0))
        .collect(Collectors.toList());
    private static final List<Post> testVisiblePostList = LongStream.rangeClosed(1, TEST_SIZE)
        .mapToObj(id -> EntityCreator.post(id, testCategory, "title", "content", "desc", true))
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
        @DisplayName("성공: 페이지, 카테고리에 맞는 포스트를 반환한다.")
        void Success_GetPostsByCategory_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(postService.getPostsByCategory(anyLong(), any(Pageable.class))).willReturn(testPostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CATEGORYID, "" + testCategory.getIdx())
            );

            // then
            verify(postService).getPostsByCategory(anyLong(), any(Pageable.class));
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
                eq(true), anyLong(), any(Pageable.class)
            )).willReturn(testVisiblePostList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CATEGORYID, "" + testCategory.getIdx())
                .param(QUERY_STRING_VISIBLE, "true")
            );

            // then
            verify(postService).getPostsByVisibleAndCategory(
                eq(true), anyLong(), any(Pageable.class)
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
        @DisplayName("실패: 존재하지 않는 카테고리의 포스트 요청")
        void Fail_PostsOfNonExistentCategory_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(postService.getPostsByCategory(anyLong(), any(Pageable.class))).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_CATEGORYID, "99999")
            );

            // then
            verify(postService).getPostsByCategory(anyLong(), any(Pageable.class));
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
            PostDto.Req inputDto = DtoCreator.postReqDto(testCategory.getIdx(), "title", "content", "desc", true);
            Post createdEntity = EntityCreator.post(1l, testCategory, "title", "content", "desc", true);

            given(postService.createPost(any(PostDto.Req.class))).willReturn(createdEntity);

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(postService).createPost(any(PostDto.Req.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("createPost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.id", is((int) createdEntity.getIdx().longValue())),
                jsonPath("$.response.category.name", is(createdEntity.getCategory().getName())),
                jsonPath("$.response.category.parent", is(createdEntity.getCategory().getParentCategory().getName())),
                jsonPath("$.response.title", is(createdEntity.getTitle())),
                jsonPath("$.response.content", is(createdEntity.getContent())),
                jsonPath("$.response.desc", is(createdEntity.getDesc())),
                jsonPath("$.response.visible", is(createdEntity.getVisible()))
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
            Post returnEntity = EntityCreator.post(1l, testCategory, "title", "content", "desc", true);
            given(postService.getPost(anyLong())).willReturn(returnEntity);

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
                jsonPath("$.response.id", is((int) returnEntity.getIdx().longValue())),
                jsonPath("$.response.category.name", is(returnEntity.getCategory().getName())),
                jsonPath("$.response.category.parent", is(returnEntity.getCategory().getParentCategory().getName())),
                jsonPath("$.response.title", is(returnEntity.getTitle())),
                jsonPath("$.response.content", is(returnEntity.getContent())),
                jsonPath("$.response.desc", is(returnEntity.getDesc())),
                jsonPath("$.response.visible", is(returnEntity.getVisible()))
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
            ErrorMessage errorMessage = ErrorMessage.INVALID_PARAM;
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
            PostDto.Req inputPost = DtoCreator.postReqDto(testCategory.getIdx(), "title", "content", "desc", true);
            Post updatedEntity = EntityCreator.post(1l, testCategory, "title", "content", "desc", true);
            given(postService.updatePost(anyLong(), any(PostDto.Req.class))).willReturn(updatedEntity);

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPost))
            );

            // when
            verify(postService).updatePost(anyLong(), any(PostDto.Req.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(PostRestController.class),
                handler().methodName("updatePost"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.id", is((int) updatedEntity.getIdx().longValue())),
                jsonPath("$.response.category", is(notNullValue())),
                jsonPath("$.response.category.name", is(updatedEntity.getCategory().getName())),
                jsonPath("$.response.category.parent", is(updatedEntity.getCategory().getParentCategory().getName())),
                jsonPath("$.response.title", is(updatedEntity.getTitle())),
                jsonPath("$.response.content", is(updatedEntity.getContent())),
                jsonPath("$.response.desc", is(updatedEntity.getDesc())),
                jsonPath("$.response.visible", is(updatedEntity.getVisible()))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 수정")
        void Fail_NonExistentPost_ReturnApiResultOfException() throws Exception {
            // given
            PostDto.Req inputDto = DtoCreator.postReqDto(1l, "title", "content", "desc", true);
            ErrorMessage errorMessage = ErrorMessage.NO_CHANGES;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(postService.updatePost(anyLong(), any(PostDto.Req.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(postService).updatePost(anyLong(), any(PostDto.Req.class));
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
