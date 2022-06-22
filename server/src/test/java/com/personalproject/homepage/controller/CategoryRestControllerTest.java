
package com.personalproject.homepage.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.service.CategoryService;

@WebMvcTest(CategoryRestController.class)
@CustomUnitTestSecurityConfig
@WithMockUser(roles = "ADMIN") // 모든 접근 권한을 가진 MockUser를 설정 한다.
@ActiveProfiles("test")
public class CategoryRestControllerTest {

    private static final String ROOT = "/api/categories";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf8";
    private static final String QUERY_STRING_LVL = "lvl";
    private static final String QUERY_STRING_NAME = "name";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CategoryDto testTopLevelCategory = CategoryDto.builder().name("name").build();
    private static final CategoryDto testSubCategory = CategoryDto.builder().name("name").parent("parent").build();
    private static final List<CategoryDto> allCategoryTestList = List.of(
        CategoryDto.builder().name("p1").build(),
        CategoryDto.builder().name("p1").parent("p1").build(),
        CategoryDto.builder().name("c1").parent("p1").build(),
        CategoryDto.builder().name("c2").parent("p1").build()
    );
    private static final List<CategoryDto> topCategoryTestList = List.of(
        CategoryDto.builder().name("p1").build(),
        CategoryDto.builder().name("p2").build(),
        CategoryDto.builder().name("p3").build()
    );
    private static final List<CategoryDto> subCategoryTestList = List.of(
        CategoryDto.builder().name("c1").parent("p1").build(),
        CategoryDto.builder().name("c2").parent("p1").build(),
        CategoryDto.builder().name("c3").parent("p1").build()
    );

    @Autowired private MockMvc mockMvc;

    @MockBean CategoryService categoryService;

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
        @DisplayName("성공: 모든 최상위 카테고리를 조회한다.")
        void Success_GetTopLevelCategories_ReturnApiResultOfDtoList() throws Exception {
            // given - topCategoryTestList
            given(categoryService.getAllTopLevelCategories()).willReturn(topCategoryTestList);

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_LVL, "top")
            );

            // then
            verify(categoryService).getAllTopLevelCategories();
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", is(topCategoryTestList.size())),
                jsonPath("$.response[?(@.name == null)]", is(empty())),
                jsonPath("$.response[?(@.parent != null)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("성공: 카테고리의 모든 하위 카테고리를 조회한다.")
        void Success_GetSubCategories_ReturnApiResultOfDtoList() throws Exception {
            // given
            given(categoryService.getAllSubCategoriesOf(any(CategoryDto.class))).willReturn(subCategoryTestList);

            // when
            ResultActions result =
            mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_LVL, "sub")
                .param(QUERY_STRING_NAME, "p1")
            );

            // then
            verify(categoryService).getAllSubCategoriesOf(any(CategoryDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.length()", is(subCategoryTestList.size())),
                jsonPath("$.response[?(@.name == null)]", is(empty())),
                jsonPath("$.response[?(@.parent == null)]", is(empty()))
            ));
        }

        @Test
        @DisplayName("실패: 최상위 카테고리 조회 시 유효하지 않은 query string - lvl")
        void Fail_InvalidQuery_GetTopLevelCategories_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.INVALID_QUERY_STRING;
            String message = errorMessage.getMessage("lvl", "top, sub");
            int status = errorMessage.getStatus().value();

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_LVL, "invalid")
            );

            // then
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }

        @Test
        @DisplayName("실패: 하위 카테고리 조회 시 유효하지 않은 query string - name")
        void Fail_InvalidQuery_GetSubCategories_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.EMPTY_QUERY_STRING;
            String message = errorMessage.getMessage("name");
            int status = errorMessage.getStatus().value();

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_LVL, "sub")
                .param(QUERY_STRING_NAME, " ") // empty string
            );

            // then
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(notNullValue())),
                jsonPath("$.error.message", is(message)),
                jsonPath("$.error.status", is(status))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 하위 카테고리 조회")
        void Fail_NoParent_GetSubCategories_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("상위 카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.getAllSubCategoriesOf(any(CategoryDto.class)))
                .willThrow(new ApiException(errorMessage, "상위 카테고리"));

            // when
            ResultActions result = mockMvc.perform(get(ROOT)
                .param(QUERY_STRING_LVL, "sub")
                .param(QUERY_STRING_NAME, "nonExistent")
            );

            // then
            verify(categoryService).getAllSubCategoriesOf(any(CategoryDto.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("getCategories"),
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
    @DisplayName("POST /api/categories")
    class Test_Post_Category {
        @Test
        @DisplayName("성공: 카테고리를 추가한다.")
        void Success_NewCategory_ReturnApiResultOfDto() throws Exception {
            // given - testTOplevelCategory
            CategoryDto category = testTopLevelCategory;
            given(categoryService.createCategory(any(CategoryDto.class))).willReturn(category);

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category))
            );

            // then
            verify(categoryService).createCategory(any(CategoryDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("createCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.name", is(category.getName())),
                jsonPath("$.response.parent", is(category.getParent()))
            ));
        }

        @Test
        @DisplayName("실패: 같은 카테고리를 추가")
        void Fail_ExistentCategory_ReturnApiResultOfException() throws Exception {
            // given - testTopLevelCategory
            ErrorMessage errorMessage = ErrorMessage.ALREADY_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.createCategory(any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTopLevelCategory))
            );

            // when
            verify(categoryService).createCategory(any(CategoryDto.class));
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
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("상위 카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.createCategory(any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage, "상위 카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(post(ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSubCategory))
            );

            // then
            verify(categoryService).createCategory(any(CategoryDto.class));
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
    @DisplayName("PATCH /api/categories/{name}")
    class Test_Patch_TopLevelCategory {
        @Test
        @DisplayName("성공: 최상위 카테고리를 수정한다.")
        void Success_PatchTopLevelCategory_ReturnApiResultOfDto() throws Exception {
            // given
            CategoryDto updatedCategory = CategoryDto.builder().name("changed").build();
            given(categoryService.updateCategory(any(CategoryDto.class), any(CategoryDto.class))).willReturn(updatedCategory);

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCategory))
            );

            // then
            result.andDo(print());
            verify(categoryService).updateCategory(any(CategoryDto.class), any(CategoryDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("updateCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.name", is(updatedCategory.getName())),
                jsonPath("$.response.parent", is(updatedCategory.getParent()))
            ));
        }

        @Test
        @DisplayName("실패: 변경 사항이 없다.")
        void Fail_NoChanges_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NO_CHANGES;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(categoryService.updateCategory(any(CategoryDto.class), any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/" + testTopLevelCategory.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTopLevelCategory))
            );

            // then
            verify(categoryService).updateCategory(any(CategoryDto.class), any(CategoryDto.class));
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
    @DisplayName("DELETE /api/categories/{name}")
    class Test_Delete_TopLevelCategory {
        @Test
        @DisplayName("성공: 최상위 카테고리를 삭제한다.")
        void Success_DeleteTopLevelCategory_ReturnApiResultOfTrue() throws Exception {
            // given
            given(categoryService.deleteCategory(any(CategoryDto.class))).willReturn(true);

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/name"));

            // then
            verify(categoryService).deleteCategory(any(CategoryDto.class));
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
        @DisplayName("실패: 존재하지 않는 최상위 카테고리를 삭제한다.")
        void Fail_NonExistentTopLevelCategory_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.deleteCategory(any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/nonExistent"));

            // then
            verify(categoryService).deleteCategory(any(CategoryDto.class));
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
            given(categoryService.deleteCategory(any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/name"));

            // then
            verify(categoryService).deleteCategory(any(CategoryDto.class));
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

    @Nested
    @DisplayName("PATCH /api/categories/{parent}/{name}")
    class Test_Patch_SubCategory {
        @Test
        @DisplayName("성공: 하위 카테고리를 수정한다.")
        void Success_PatchSubCategory_ReturnApiResultOfDto() throws Exception {
            // given
            CategoryDto updatedCategory = CategoryDto.builder().name("changed").parent("parent").build();
            given(categoryService.updateCategory(any(CategoryDto.class), any(CategoryDto.class))).willReturn(updatedCategory);

            // when
            ResultActions result = mockMvc.perform(patch(ROOT + "/parent/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCategory))
            );

            // then
            verify(categoryService).updateCategory(any(CategoryDto.class), any(CategoryDto.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(CategoryRestController.class),
                handler().methodName("updateCategory"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(notNullValue())),
                jsonPath("$.response.name", is(updatedCategory.getName())),
                jsonPath("$.response.parent", is(updatedCategory.getParent()))
            ));
        }

        @Test
        @DisplayName("실패: 변경 사항이 없다.")
        void Fail_NoChanges_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NO_CHANGES;
            String message = errorMessage.getMessage();
            int status = errorMessage.getStatus().value();
            given(categoryService.updateCategory(any(CategoryDto.class), any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage)
            );

            // when
            ResultActions result = mockMvc.perform(patch(String.format("%s/%s/%s", ROOT, testSubCategory.getParent(), testSubCategory.getName()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSubCategory))
            );

            // then
            verify(categoryService).updateCategory(any(CategoryDto.class), any(CategoryDto.class));
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
    @DisplayName("DELETE /api/categories/{parent}/{name}")
    class Test_Delete_SubCategory {
        @Test
        @DisplayName("성공: 하위 카테고리를 삭제한다.")
        void Success_DeleteSubCategory_ReturnApiResultOfTrue() throws Exception {
            // given
            given(categoryService.deleteCategory(any(CategoryDto.class))).willReturn(true);

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/parent/name"));

            // then
            verify(categoryService).deleteCategory(any(CategoryDto.class));
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
        @DisplayName("실패: 존재하지 않는 하위 카테고리를 삭제한다.")
        void Fail_NonExistentSubCategory_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            String message = errorMessage.getMessage("카테고리");
            int status = errorMessage.getStatus().value();
            given(categoryService.deleteCategory(any(CategoryDto.class))).willThrow(
                new ApiException(errorMessage, "카테고리")
            );

            // when
            ResultActions result = mockMvc.perform(delete(ROOT + "/parent/nonExistent"));

            // then
            verify(categoryService).deleteCategory(any(CategoryDto.class));
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
