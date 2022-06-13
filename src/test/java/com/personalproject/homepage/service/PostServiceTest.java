package com.personalproject.homepage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.dto.PostDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.MockEntity;
import com.personalproject.homepage.mapper.CategoryMapper;
import com.personalproject.homepage.mapper.PostMapper;
import com.personalproject.homepage.repository.PostRepository;

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

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PostServiceTest {

    private static final int TEST_PAGE = 0;
    private static final int TEST_SIZE = 8;
    private static final Sort testSort = Sort.by(Direction.DESC, "createAt");
    private static final Pageable testPageable = PageRequest.of(TEST_PAGE, TEST_SIZE, testSort);

    @Mock private PostRepository postRepository;
    @Mock private CategoryMapper categoryMapper;

    private PostMapper postMapper;
    private PostService postService;
    private Category testCategoryEntity;
    private Post testPostEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postMapper = new PostMapper(postRepository, categoryMapper);
        postService = new PostService(postRepository, postMapper, categoryMapper);

        testCategoryEntity = MockEntity.mock(Category.class, 99L);
        testCategoryEntity.updateInfo("category", null);
        testPostEntity = MockEntity.mock(Post.class, 99L);
        testPostEntity.updateInfo(testCategoryEntity, "title", "content", true);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Post {
        @Test
        @DisplayName("성공: 카테고리가 없는 포스트를 추가하고 dto를 반환한다.")
        void Success_NewPostWithNoCategory_ReturnDto() {
            // given - testPostEntity
            PostDto inputDto = PostDto.builder()
                .title("title")
                .content("content")
                .visible(true)
                .build();
            given(postRepository.save(any(Post.class))).willReturn(testPostEntity);

            // when
            PostDto createdDto = postService.createPost(inputDto);

            // then
            verify(postRepository).save(any(Post.class));
            assertThat(createdDto)
                .extracting("id")
                .isNotNull();
        }

        @Test
        @DisplayName("성공: 카테고리가 있는 포스트를 추가하고 dto를 반환한다.")
        void Success_NewPostWithCategory_ReturnDto() {
            // given - testCategoryEntity, testPostEntity
            String categoryName = "category";
            CategoryDto categoryDto = CategoryDto.builder().name(categoryName).build();
            PostDto inputDto = PostDto.builder()
                .category(categoryDto)
                .title("title")
                .content("content")
                .visible(true)
                .build();

            given(categoryMapper.CategoryDtoToEntity(categoryDto)).willReturn(testCategoryEntity);
            given(postRepository.save(any(Post.class))).willReturn(testPostEntity);
            given(categoryMapper.entityToCategoryDto(testCategoryEntity)).willReturn(categoryDto);

            // when
            PostDto createdDto = postService.createPost(inputDto);

            // then
            verify(categoryMapper).CategoryDtoToEntity(categoryDto);
            verify(postRepository).save(any(Post.class));
            verify(categoryMapper).entityToCategoryDto(testCategoryEntity);
            assertThat(createdDto)
                .extracting("id")
                .isNotNull();
            assertThat(createdDto)
                .extracting("category.name")
                .isEqualTo(categoryName);
        }
    }

    @Nested
    @DisplayName("Read")
    class Test_Read_Post {
        @Test
        @DisplayName("성공: id에 맞는 포스트를 반환한다.")
        void Success_OnePostById_ReturnDto() {
            // given - testPostEntity
            Long id = testPostEntity.getIdx();
            given(postRepository.findById(id)).willReturn(Optional.of(testPostEntity));

            // when
            PostDto returnDto = postService.getPost(id);

            // then
            verify(postRepository).findById(id);
            assertThat(returnDto)
                .extracting("id")
                .isNotNull();
        }

        @Test
        @DisplayName("실패: id에 맞지 않는 포스트 요청 시 예외를 던진다.")
        void Fail_OnePostByInvalidId_ThrowException() {
            // given
            Long id = -1L; // invalid idx
            given(postRepository.findById(id)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.getPost(id));

            // then
            verify(postRepository).findById(id);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("포스트"));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 포스트를 dto list로 반환한다.")
        void Success_PostsByAnyCategoryPerPage_ReturnDtoList() {
            // given - testPostEntity
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            given(postRepository.findAll(testPageable)).willReturn(postEntityList);

            // when
            List<PostDto> returnDtoList = postService.getPosts(testPageable);

            // then
            verify(postRepository).findAll(testPageable);
            assertThat(returnDtoList)
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 포스트를 dto list로 반환한다.")
        void Success_PostsByOneCategoryPerPage_ReturnDtoList() {
            // given - testCategoryEntity, testPostEntity
            Category category = testCategoryEntity;
            String name = category.getName();
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            CategoryDto inputCategoryDto = CategoryDto.builder().name(name).build();
            given(categoryMapper.CategoryDtoToEntity(inputCategoryDto)).willReturn(category);
            given(postRepository.findAllByCategory(category, testPageable)).willReturn(postEntityList);
            given(categoryMapper.entityToCategoryDto(category)).willReturn(CategoryDto.builder().name(name).build());

            // when
            List<PostDto> returnDtoList = postService.getPostsByCategory(inputCategoryDto, testPageable);

            // then
            verify(categoryMapper).CategoryDtoToEntity(inputCategoryDto);
            verify(postRepository).findAllByCategory(category, testPageable);
            verify(categoryMapper, times(postEntityList.size())).entityToCategoryDto(category);
            assertThat(returnDtoList)
                .allMatch(p -> p.getCategory().getName().equals(name))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 포스트 요청 시 예외를 던진다.")
        void Fail_PostsByInvalidCategoryPerPage_ThrowException() {
            // given
            String name = "invalid";
            CategoryDto categoryDto = CategoryDto.builder().name(name).build();
            Category invalidCategory = MockEntity.mock(Category.class); // idx == null
            invalidCategory.updateInfo(name, null);
            given(categoryMapper.CategoryDtoToEntity(categoryDto)).willReturn(invalidCategory);

            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByCategory(categoryDto, testPageable));

            // then
            verify(categoryMapper).CategoryDtoToEntity(categoryDto);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible인 포스트를 dto list로 반환한다.")
        void Success_VisiblePostsByAnyCategoryPerPage_ReturnDtoList() {
            // given - testPostEntity
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            given(postRepository.findAllByVisibleTrue(testPageable)).willReturn(postEntityList);

            // when
            List<PostDto> returnDtoList = postService.getPostsByVisible(true, testPageable);

            // then
            verify(postRepository).findAllByVisibleTrue(testPageable);
            assertThat(returnDtoList)
                .allMatch(p -> p.getVisible())
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible인 포스트를 dto list로 반환한다.")
        void Success_VisiblePostsByOneCategoryPerPage_ReturnDtoList() {
            // given - testCategoryEntity, testPostEntity
            Category category = testCategoryEntity;
            String name = category.getName();
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);

            given(postRepository.findAllByVisibleTrue(testPageable)).willReturn(postEntityList);
            given(categoryMapper.entityToCategoryDto(testCategoryEntity)).willReturn(CategoryDto.builder().name(name).build());

            // when
            List<PostDto> returnDtoList = postService.getPostsByVisible(true, testPageable);

            // then
            verify(postRepository).findAllByVisibleTrue(testPageable);
            verify(categoryMapper, times(postEntityList.size())).entityToCategoryDto(testCategoryEntity);
            assertThat(returnDtoList)
                .allMatch(p -> p.getVisible() && p.getCategory().getName().equals(name))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 visible인 포스트 요청 시 예외를 던진다.")
        void Fail_VisiblePostsByInvalidCategoryPerPage_ThrowException() {
            // given
            String name = "invalid";
            CategoryDto categoryDto = CategoryDto.builder().name(name).build();
            Category invalidCategory = MockEntity.mock(Category.class);
            invalidCategory.updateInfo(name, null);
            given(categoryMapper.CategoryDtoToEntity(categoryDto)).willReturn(invalidCategory);

            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByVisibleAndCategory(true, categoryDto, testPageable));

            // then
            verify(categoryMapper).CategoryDtoToEntity(categoryDto);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 invisible인 포스트를 dto list로 반환한다.")
        void Success_InvisiblePostsByAnyCategoryPerPage_ReturnDtoList() {
            // given - testPostEntity
            testPostEntity.updateInfo(null, null, null, false);
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);
            given(postRepository.findAllByVisibleFalse(testPageable)).willReturn(postEntityList);

            // when
            List<PostDto> returnDtoList = postService.getPostsByVisible(false, testPageable);

            // then
            verify(postRepository).findAllByVisibleFalse(testPageable);
            assertThat(returnDtoList)
                .allMatch(p -> !p.getVisible())
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 invisible인 포스트를 dto list로 반환한다.")
        void Success_InvisiblePostsByOneCategoryPerPage_ReturnDtoList() {
            // given - testCategoryEntity, testPostEntity
            Category category = testCategoryEntity;
            String name = category.getName();
            testPostEntity.updateInfo(category, null, null, false);
            List<Post> postEntityList = new ArrayList<>();
            postEntityList.add(testPostEntity);

            given(postRepository.findAllByVisibleFalse(testPageable)).willReturn(postEntityList);
            given(categoryMapper.entityToCategoryDto(testCategoryEntity)).willReturn(CategoryDto.builder().name(name).build());

            // when
            List<PostDto> returnDtoList = postService.getPostsByVisible(false, testPageable);

            // then
            verify(postRepository).findAllByVisibleFalse(testPageable);
            verify(categoryMapper, times(postEntityList.size())).entityToCategoryDto(testCategoryEntity);
            assertThat(returnDtoList)
                .allMatch(p -> !p.getVisible() && p.getCategory().getName().equals(name))
                .size()
                .isBetween(0, TEST_SIZE);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리의 invisible인 포스트 요청 시 예외를 던진다.")
        void Fail_InvisiblePostsByInvalidCategoryPerPage_ThrowException() {
            // given
            String name = "invalid";
            CategoryDto categoryDto = CategoryDto.builder().name(name).build();
            Category invalidCategory = MockEntity.mock(Category.class);
            invalidCategory.updateInfo(name, null);
            given(categoryMapper.CategoryDtoToEntity(categoryDto)).willReturn(invalidCategory);

            // when
            Throwable thrown = catchThrowable(() -> postService.getPostsByVisibleAndCategory(false, categoryDto, testPageable));

            // then
            verify(categoryMapper).CategoryDtoToEntity(categoryDto);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Post {
        @Test
        @DisplayName("성공: 포스트의 내용을 변경한다.")
        void Success_PostDetailById_ReturnDto() {
            // given - testPostEntity
            String newName = "newCategory";
            String newTitle = "newTitle";
            String newContent = "newContent";
            boolean newVisible = false;
            Category newCategory = MockEntity.mock(Category.class, 1L);
            newCategory.updateInfo(newName, null);
            CategoryDto newCategoryDto = CategoryDto.builder().name(newName).build();
            PostDto postDto = PostDto.builder()
                .category(newCategoryDto)
                .title(newTitle)
                .content(newContent)
                .visible(newVisible)
                .build();
            Long id = testPostEntity.getIdx();

            given(postRepository.findById(id)).willReturn(Optional.of(testPostEntity));
            given(categoryMapper.CategoryDtoToEntity(newCategoryDto)).willReturn(newCategory);
            given(categoryMapper.entityToCategoryDto(newCategory)).willReturn(newCategoryDto);

            // when
            PostDto returnDto = postService.updatePost(id, postDto);

            // then
            verify(postRepository).findById(id);
            verify(categoryMapper).CategoryDtoToEntity(newCategoryDto);
            verify(categoryMapper).entityToCategoryDto(newCategory);
            assertThat(returnDto)
                .extracting("category.name", "title", "content", "visible")
                .containsExactly(newName, newTitle, newContent, newVisible);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 수정 시 예외를 던진다.")
        void Fail_PostDetailByInvalidId_ThrowException() {
            // given
            Long invalidId = 1L;
            PostDto postDto = PostDto.builder()
                .title("title")
                .content("content")
                .visible(true)
                .build();
            given(postRepository.findById(invalidId)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.updatePost(invalidId, postDto));

            // then
            verify(postRepository).findById(invalidId);
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
            given(postRepository.findById(id)).willReturn(Optional.of(testPostEntity));

            // when
            boolean isDeleted = postService.deletePost(id);

            // then
            verify(postRepository).findById(id);
            verify(postRepository).delete(testPostEntity);
            assertThat(isDeleted)
                .isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 포스트 삭제 시 예외를 던진다.")
        void Success_OnePostByInvalidId_ThrowException() {
            // given
            Long invalidId = 1L;
            given(postRepository.findById(invalidId)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> postService.deletePost(invalidId));

            // then
            verify(postRepository).findById(invalidId);
            verify(postRepository, times(0)).delete(any(Post.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("포스트"));
        }
    }
}
