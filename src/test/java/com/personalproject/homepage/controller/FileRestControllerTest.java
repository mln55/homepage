package com.personalproject.homepage.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.personalproject.homepage.config.CustomUnitTestSecurityConfig;
import com.personalproject.homepage.dto.FileResponseDto;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.service.FileService;

@WebMvcTest(FileRestController.class)
@CustomUnitTestSecurityConfig
@WithMockUser(roles = "ADMIN")
@ActiveProfiles("test")
public class FileRestControllerTest {

    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf8";

    @Autowired MockMvc mockMvc;

    @MockBean FileService fileService;


    @Nested
    @DisplayName("POST /api/files")
    class Test_Upload_File {
        @Test
        @DisplayName("성공: 이미지 파일을 업로드 한다.")
        void Success_ImageFile_ReturnApiResultOfDto() throws Exception {
            // given
            given(fileService.save(any(MockMultipartFile.class))).willReturn(new FileResponseDto("savedName", "url"));

            // when
            ResultActions result = mockMvc.perform(multipart("/api/files")
                .file(new MockMultipartFile(
                    "file", "file.png", "image/png", "content".getBytes())
                )
            );

            // then
            verify(fileService).save(any(MockMultipartFile.class));
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(FileRestController.class),
                handler().methodName("uploadFile"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.savedName", isA(String.class)),
                jsonPath("$.response.url", isA(String.class))
            ));
        }

        @Test
        @DisplayName("실패: 이미지 파일이 아니면 업로드 하지 않는다.")
        void Fail_NotImageFile_ReturnApiResultOfException() throws Exception {
            // given
            given(fileService.save(any(MockMultipartFile.class))).willThrow(
                new IllegalArgumentException("이미지 파일만 업로드 할 수 있습니다.")
            );

            // when
            ResultActions result = mockMvc.perform(multipart("/api/files")
                .file(new MockMultipartFile(
                    "file", "file.png", "NOT IMAGE", "content".getBytes())
                )
            );

            // then
            verify(fileService).save(any(MockMultipartFile.class));
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(FileRestController.class),
                handler().methodName("uploadFile"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(not(empty()))),
                jsonPath("$.error.status", is(400)),
                jsonPath("$.error.message", is("이미지 파일만 업로드 할 수 있습니다."))
            ));
        }
    }

    @Nested
    @DisplayName("DELETE /api/files")
    class Test_Delete_File {
        @Test
        @DisplayName("성공: 파일을 삭제한다.")
        void Success_ExistentFile_ReturnApiResultOfDto() throws Exception {
            // given
            given(fileService.delete(anyString())).willReturn(
                new FileResponseDto("savedName", "url")
            );

            // when
            ResultActions result = mockMvc.perform(delete("/api/files/existent.iamge"));

            // then
            verify(fileService).delete(anyString());
            result.andExpect(matchAll(
                status().isOk(),
                handler().handlerType(FileRestController.class),
                handler().methodName("deleteFile"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(true)),
                jsonPath("$.error", is(nullValue())),
                jsonPath("$.response", is(not(empty()))),
                jsonPath("$.response.savedName", isA(String.class)),
                jsonPath("$.response.url", isA(String.class))
            ));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 파일 삭제 요청")
        void Fail_NonExistentFile_ReturnApiResultOfException() throws Exception {
            // given
            ErrorMessage errorMessage = ErrorMessage.NON_EXISTENT;
            int status = errorMessage.getStatus().value();
            String message = errorMessage.getMessage("파일");
            given(fileService.delete(anyString())).willThrow(new ApiException(errorMessage, "파일"));

            // when
            ResultActions result = mockMvc.perform(delete("/api/files/nonexistent"));

            // then
            verify(fileService).delete(anyString());
            result.andExpect(matchAll(
                status().isBadRequest(),
                handler().handlerType(FileRestController.class),
                handler().methodName("deleteFile"),
                content().contentType(JSON_CONTENT_TYPE),
                jsonPath("$.success", is(false)),
                jsonPath("$.response", is(nullValue())),
                jsonPath("$.error", is(not(empty()))),
                jsonPath("$.error.status", is(status)),
                jsonPath("$.error.message", is(message))
            ));
        }
    }
}
