package com.personalproject.homepage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;

import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.DtoCreator;
import com.personalproject.homepage.helper.EntityCreator;
import com.personalproject.homepage.repository.CategoryRepository;
import com.personalproject.homepage.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PostServiceTest {

    private static final int TEST_PAGE = 0;
    private static final int TEST_SIZE = 8;
    private static final Sort testSort = Sort.by(Direction.DESC, "createAt");
    private static final Pageable testPageable = PageRequest.of(TEST_PAGE, TEST_SIZE, testSort);

    private static final Category testParentCategoryEntity = EntityCreator.category(99l, "parent", null);

    @Mock private PostRepository postRepository;
    @Mock private CategoryRepository categoryRepository;

    private PostService postService;
    private Category testCategoryEntity;
    private Post testPostEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postService = new PostService(postRepository, categoryRepository);

        testCategoryEntity = EntityCreator.category(100l, "category", testParentCategoryEntity);
        testPostEntity = EntityCreator.post(99l, testCategoryEntity, "title", "content", "desc", true);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Post {
        @Test
        @DisplayName("성공: 포스트를 추가하고 entity를 반환한다.")
        void Success_NewPost_ReturnEntity() {
            // given - testCategoryEntity, testPostEntity
            Post entity = testPostEntity;
            Long categoryId = entity.getCategory().getIdx();
            PostDto.Req inputDto = DtoCreator.postReqDto(
                categoryId,
                entity.getTitle(),
                entity.getContent(),
                entity.getDesc(),
                entity.getVisible()
            );
            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(testCategoryEntity));
            given(postRepository.save(any(Post.class))).willReturn(testPostEntity);

            // when
            Post createdEntity = postService.createPost(inputDto);

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(postRepository).save(any(Post.class));
            assertThat(createdEntity)
                .extracting("idx")
                .isNotNull();
        }
    }

    @Nested
    @DisplayName("Read")
    class Test_Read_Post {
        @Test
        @DisplayName("성공: id에 맞는 포스트를 반환한다.")
        void Success_OnePostById_ReturnEntity() {
            // given - testPostEntity
            Long id = testPostEntity.getIdx();
            given(postRepository.findById(eq(id))).willReturn(Optional.of(testPostEntity));

            // when
            Post returnEntity = postService.getPost(id);

            // then
            verify(postRepository).findById(eq(id));
            assertThat(returnEntity)
                .extracting("idx")
                .isNotNull();
        }

        @Test
        @DisplayName("실패: id에 맞지 않는 포스트 요청 시 예외를 던진다.")
        void Fail_OnePostByInvalidId_ThrowException() {
            // given
            Long id = -1L; // invalid idx
            given(postRepository.findById(eq(id))).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.getPost(id));

            // then
            verify(postRepository).findById(eq(id));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("포스트"));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 포스트를 entity list로 반환한다.")
        void Success_PostsByAnyCategoryPerPage_ReturnEntityList() {
            // given - testPostEntity
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            given(postRepository.findAll(eq(testPageable))).willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPosts(testPageable);

            // then
            verify(postRepository).findAll(eq(testPageable));
            assertThat(returnEntityList)
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 포스트를 entity list로 반환한다.")
        void Success_PostsByOneCategoryPerPage_ReturnEntityList() {
            // given - testCategoryEntity, testPostEntity
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            Long categoryId = testCategoryEntity.getIdx();
            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(testCategoryEntity));
            given(postRepository.findAllByCategory(eq(testCategoryEntity), eq(testPageable))).willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPostsByCategory(categoryId, testPageable);

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(postRepository).findAllByCategory(eq(testCategoryEntity), eq(testPageable));
            assertThat(returnEntityList)
                .allSatisfy(p -> assertThat(p)
                    .extracting("category")
                    .isEqualTo(testCategoryEntity))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 포스트 요청 시 예외를 던진다.")
        void Fail_PostsByInvalidCategoryPerPage_ThrowException() {
            // given
            Long invalidCategoryId = -1l;
            given(categoryRepository.findById(eq(invalidCategoryId))).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "카테고리")
            );
            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByCategory(invalidCategoryId, testPageable));

            // then
            verify(categoryRepository).findById(eq(invalidCategoryId));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible인 포스트를 entity list로 반환한다.")
        void Success_VisiblePostsByAnyCategoryPerPage_ReturnEntityList() {
            // given - testPostEntity
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            Boolean visible = true;
            given(postRepository.findAllByVisible(eq(visible), eq(testPageable))).willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPostsByVisible(true, testPageable);

            // then
            verify(postRepository).findAllByVisible(eq(visible), eq(testPageable));
            assertThat(returnEntityList)
                .allMatch(p -> p.getVisible())
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible인 포스트를 entity list로 반환한다.")
        void Success_VisiblePostsByOneCategoryPerPage_ReturnEntityList() {
            // given - testCategoryEntity, testPostEntity
            boolean visible = true;
            Long categoryId = testCategoryEntity.getIdx();
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(testCategoryEntity));
            given(postRepository.findAllByVisibleAndCategory(eq(visible), eq(testCategoryEntity), eq(testPageable))).willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPostsByVisibleAndCategory(visible, categoryId, testPageable);

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(postRepository).findAllByVisibleAndCategory(eq(visible), eq(testCategoryEntity), eq(testPageable));
            assertThat(returnEntityList)
                .allSatisfy(p -> assertThat(p)
                    .extracting("visible", "category")
                    .containsExactly(visible, testCategoryEntity))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 visible인 포스트 요청 시 예외를 던진다.")
        void Fail_VisiblePostsByInvalidCategoryPerPage_ThrowException() {
            // given
            Long invalidCategoryId = -1l;
            given(categoryRepository.findById(eq(invalidCategoryId))).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "카테고리")
            );
            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByVisibleAndCategory(true, invalidCategoryId, testPageable));

            // then
            verify(categoryRepository).findById(eq(invalidCategoryId));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 상위 카테고리에 속한 포스트 리스트를 반환한다.")
        void Success_PostsByCategoriesOfParentCategoryPerPage_ReturnEntityList() {
            // given
            Category parentCategory = testParentCategoryEntity;
            Category childCategory = testCategoryEntity;
            Long categoryId = parentCategory.getIdx();
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);

            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(parentCategory));
            given(postRepository.findAllIncludeChildCategory(eq(parentCategory), eq(testPageable))).willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPostsByCategory(categoryId, testPageable);

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(postRepository).findAllIncludeChildCategory(eq(parentCategory), eq(testPageable));
            assertThat(returnEntityList)
                .allSatisfy(p -> assertThat(p)
                    .extracting("category")
                    .isEqualTo(childCategory))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상위 카테고리에 속한 포스트 요청 시 예외를 던진다.")
        void Fail_PostsByCategoriesOfInvalidParentCategoryPerPage_ThrowException() {
            // given
            Long invalidCategoryId = -1l;
            given(categoryRepository.findById(eq(invalidCategoryId))).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "카테고리")
            );

            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByCategory(invalidCategoryId, testPageable));

            // then
            verify(categoryRepository).findById(eq(invalidCategoryId));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 상위 카테고리에 속한 visible 포스트 리스트를 반환한다.")
        void Success_VisiblePostsByCategoriesOfParentCategoryPerPage_ReturnEntityList() {
            // given
            Category parentCategory = testParentCategoryEntity;
            Long categoryId = parentCategory.getIdx();
            Category childCategory = testCategoryEntity;
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            boolean visible = true;

            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(parentCategory));
            given(postRepository.findAllVisibleIncludeChildCategory(eq(visible), eq(parentCategory), eq(testPageable)))
                .willReturn(postEntityList);

            // when
            List<Post> returnEntityList = postService.getPostsByVisibleAndCategory(visible, categoryId, testPageable);

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(postRepository).findAllVisibleIncludeChildCategory(eq(visible), eq(parentCategory), eq(testPageable));
            assertThat(returnEntityList)
                .allSatisfy(p -> assertThat(p)
                    .extracting("visible", "category")
                    .containsExactly(visible, childCategory))
                .size()
                .isBetween(0, TEST_SIZE);
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Post {
        @Test
        @DisplayName("성공: 포스트의 내용을 변경한다.")
        void Success_PostDetailById_ReturnEntity() {
            // given - testPostEntity
            String newTitle = "newTitle";
            String newContent = "newContent";
            String newDesc = "newDesc";
            boolean newVisible = false;
            PostDto.Req postDto = DtoCreator.postReqDto(
                1l,
                newTitle,
                newContent,
                newDesc,
                newVisible
            );
            Long id = testPostEntity.getIdx();
            given(postRepository.findById(eq(id))).willReturn(Optional.of(testPostEntity));
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(testPostEntity.getCategory()));

            // when
            Post returnEntity = postService.updatePost(id, postDto);

            // then
            verify(postRepository).findById(eq(id));
            verify(categoryRepository).findById(anyLong());
            assertThat(returnEntity)
                .extracting("category", "title", "content", "desc", "visible")
                .containsExactly(testPostEntity.getCategory(), newTitle, newContent, newDesc, newVisible);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 수정 시 예외를 던진다.")
        void Fail_PostDetailByInvalidId_ThrowException() {
            // given
            Long invalidId = 1L;
            given(postRepository.findById(eq(invalidId))).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.updatePost(invalidId, any(PostDto.Req.class)));

            // then
            verify(postRepository).findById(eq(invalidId));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("포스트"));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Test_Delete_Post {
        @Test
        @DisplayName("성공: id에 맞는 포스트 하나를 삭제하고 true를 반환한다.")
        void Success_OnePostById_ReturnTrue() {
            // given - testPostEntity
            Long id = testPostEntity.getIdx();
            given(postRepository.findById(eq(id))).willReturn(Optional.of(testPostEntity));

            // when
            boolean isDeleted = postService.deletePost(id);

            // then
            verify(postRepository).findById(eq(id));
            verify(postRepository).delete(testPostEntity);
            assertThat(isDeleted)
                .isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 삭제 시 예외를 던진다.")
        void Success_OnePostByInvalidId_ThrowException() {
            // given
            Long invalidId = 1L;
            given(postRepository.findById(eq(invalidId))).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.deletePost(invalidId));

            // then
            verify(postRepository).findById(eq(invalidId));
            verify(postRepository, times(0)).delete(any(Post.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("포스트"));
        }
    }
}
