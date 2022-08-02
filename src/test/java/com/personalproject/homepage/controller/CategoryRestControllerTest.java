
package com.personalproject.homepage.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalproject.homepage.config.CustomUnitTestSecurityConfig;
import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.DtoCreator;
import com.personalproject.homepage.helper.EntityCreator;
import com.personalproject.homepage.service.CategoryService;

@WebMvcTest(CategoryRestController.class)
@CustomUnitTestSecurityConfig
@WithMockUser(roles = "ADMIN") // 모든 접근 권한을 가진 MockUser를 설정 한다.
@ActiveProfiles("test")
public class CategoryRestControllerTest {

    private static final String ROOT = "/api/categories";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf8";
    private static final String QUERY_STRING_COUNT = "count";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Category testTopLevelCategory;
    private static Category testSubCategory;
    private static List<Category> allCategoryTestList;

    @Autowired private MockMvc mockMvc;

    @MockBean private CategoryService categoryService;

    @BeforeEach
    void resetMember() {

        testTopLevelCategory = EntityCreator.category(99l, "parent", null);
        testSubCategory = EntityCreator.category(100l, "name", testTopLevelCategory);

        Category p1 = EntityCreator.category(101l, "p1", null);
        allCategoryTestList = List.of(
            p1,
            EntityCreator.category(102l, "c1", p1),
            EntityCreator.category(103l, "c2", p1),
            EntityCreator.category(104l, "c3", p1)
        );
    }

    @Nested
    @DisplayName("GET /api/categories")
    class Test_Get_Categories {
        @Test
        @DisplayName("성공: 모든 카테고리를 조회한다")
        void Success_GetCategories_ReturnApiResultOfDtoList() throws Exception {
            // given - allCategoryTestList
            given(categoryService.getAllCategories()).willReturn(allCategoryTestList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT));

            // then
            verify(categoryService).getAllCategories();
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", is(allCategoryTestList.size())),
                jsonPath("$.response[?(@.name == null)]", is(empty()))
            ));

        }

        @Test
        @DisplayName("성공: 모든 카테고리를 포스트 개수와 함께 조회한다.")
        void Success_GetCategoriesWithPostsCount_ReturnApiResultOfDtoList() throws Exception {
            // given - topCategoryTestList
            Boolean visible = null;
            Category.WithPostsCount pc = new Category.WithPostsCount(testTopLevelCategory, 3l);
            List<Category.WithPostsCount> entityList = List.of(
                pc,
                new Category.WithPostsCount(testSubCategory, 5l)
            );
            given(categoryService.getAllCategoriesWithPostsCount(eq(visible))).willReturn(entityList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .queryParam(QUERY_STRING_COUNT, "post")
            );

            // then
            verify(categoryService).getAllCategoriesWithPostsCount(eq(visible));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategoriesWithCount"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", is(1)),
                jsonPath("$.response[?(@.categoryId == null)]", is(empty())),
                jsonPath("$.response[?(@.postsCount == null)]", is(empty())),
                jsonPath("$.response[0].childList.length()", is(1))
            ));
        }
    }

    @Nested
    @DisplayName("POST /api/categories")
    class Test_Post_Category {
        @Test
        @DisplayName("성공: 카테고리를 추가한다.")
        void Success_NewCategory_ReturnApiResultOfDto() throws Exception {
            // given - testTOplevelCategory
            Category category = testTopLevelCategory;
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(testTopLevelCategory.getName(), null);

            given(categoryService.createCategory(any(CategoryDto.Req.class))).willReturn(category);

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(categoryService).createCategory(any(CategoryDto.Req.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("createCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.name", is(category.getName())),
                jsonPath("$.response.parent", is(nullValue()))
            ));
        }

        @Test
        @DisplayName("실패: 같은 카테고리를 추가")
        void Fail_ExistentCategory_ReturnApiResultOfException() throws Exception {
            // given - testTopLevelCategory
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto("duplicated", null);
            ErrorMessage errorMessage = ErrorMessage.ALREADY_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.createCategory(any(CategoryDto.Req.class))).willThrow(
                new ApiException(errorMessage, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // when
            verify(categoryService).createCategory(any(CategoryDto.Req.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("createCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상위 카테고리에 카테고리 추가")
        void Fail_NonExistentParent_ReturnApiResultOfException() throws Exception {
            // given - testSubCategory
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto("name", -1l);
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("상위 카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.createCategory(any(CategoryDto.Req.class))).willThrow(
                new ApiException(errorMessage, "상위 카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(categoryService).createCategory(any(CategoryDto.Req.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("createCategory"),
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
    @DisplayName("PATCH /api/categories/{id}")
    class Test_Patch_TopLevelCategory {
        @Test
        @DisplayName("성공: 카테고리를 수정한다.")
        void Success_PatchCategory_ReturnApiResultOfDto() throws Exception {
            // given
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto("changed", null);
            Category changedCategory = testSubCategory;
            changedCategory.updateInfo("changed", null);

            given(categoryService.updateCategory(anyLong(), any(CategoryDto.Req.class))).willReturn(changedCategory);

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(categoryService).updateCategory(anyLong(), any(CategoryDto.Req.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("updateCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.name", is(inputDto.getName())),
                jsonPath("$.response.parent", is(nullValue()))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리를 수정한다.")
        void Fail_NoChanges_ReturnApiResultOfException() throws Exception {
            // given
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto("changed", null);
            ErrorMessage errorMessage = ErrorMessage.NO_CHANGES;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(categoryService.updateCategory(anyLong(), any(CategoryDto.Req.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
            );

            // then
            verify(categoryService).updateCategory(anyLong(), any(CategoryDto.Req.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("updateCategory"),
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
    @DisplayName("DELETE /api/categories/{id}")
    class Test_Delete_TopLevelCategory {
        @Test
        @DisplayName("성공: 카테고리를 삭제한다.")
        void Success_DeleteCategory_ReturnApiResultOfTrue() throws Exception {
            // given
            given(categoryService.deleteCategory(anyLong())).willReturn(true);

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/1"));

            // then
            verify(categoryService).deleteCategory(anyLong());
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("deleteCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response", is(true))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리를 삭제한다.")
        void Fail_NonExistentCategory_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.deleteCategory(anyLong())).willThrow(
                new ApiException(errorMessage, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/99999"));

            // then
            verify(categoryService).deleteCategory(anyLong());
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("deleteCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }

        @Test
        @DisplayName("실패: 포스트가 있는 카테고리를 삭제한다.")
        void Fail_CategoryThatHasPosts_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NOT_REMOVEABLE_CATEGORY;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(categoryService.deleteCategory(anyLong())).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/1"));

            // then
            verify(categoryService).deleteCategory(anyLong());
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("deleteCategory"),
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
