package com.personalproject.homepage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;
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
import org.springframework.test.context.ActiveProfiles;

import com.personalproject.homepage.dto.CategoryDto;
import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.DtoCreator;
import com.personalproject.homepage.helper.EntityCreator;
import com.personalproject.homepage.repository.CategoryRepository;

/********************************************************************************
    https://javadoc.io/static/org.mockito/mockito-core/3.9.0/org/mockito/Mockito.html

    @ExtendWith(MockitoExtension.class) - test에 mockito를 사용한다.
    @Mock - mock 객체 주입. 각 테스트에서 함수 호출에 따른 결과값을 지정해야 한다.

    spring context를 load하지 않는다.
    본 테스트에서는 mocking한 객체의 행위에 대한 결과를 지정 한 후
    원하는 테스트 결과를 얻었는지, 의도한 행위를 실행했는 지를 테스트한다.
********************************************************************************/
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    private Category testParentCategoryEntity;
    private Category testChildCategoryEntity;

    @BeforeEach
    void setUp() {
        /********************************************************************************
            mapper의 경우 service 내부의 중복된 코드를 줄이기 위한 목적이 크고
            mapper 또한 repository를 의존하므로 실제 객체를 사용한다.
        ********************************************************************************/
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoryRepository);

        testParentCategoryEntity = EntityCreator.category(99l, "testParent", null);
        testChildCategoryEntity = EntityCreator.category(100l, "testChild", testParentCategoryEntity);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Category {
        @Test
        @DisplayName("성공: 최상위 카테고리를 추가하고 entity를 반환한다.")
        void Success_NewTopLevelCategory_ReturnEntity() {
            // given
            Category entity = testParentCategoryEntity;
            String name = entity.getName();
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(name, null);
            given(categoryRepository.existsByNameAndParentCategory(name, null)).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(entity);

            // when
            Category returnEntity = categoryService.createCategory(inputDto);

            // then
            verify(categoryRepository).existsByNameAndParentCategory(name, null);
            verify(categoryRepository).save(any(Category.class));
            assertThat(returnEntity)
                .extracting("name", "parentCategory")
                .containsExactly(name, null);
        }

        @Test
        @DisplayName("실패: 카테고리 name이 null일 경우 예외를 던진다.")
        void Fail_NullNameCategory_ThrowException() {
            // given
            String name = null;
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(name, null);

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository, times(0)).delete(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.EMPTY_STRING.getMessage("name"));
        }

        @Test
        @DisplayName("실패: 중복된 최상위 카테고리 추가 시 예외를 던진다.")
        void Fail_DuplicatedTopLevelCategory_ThrowException() {
            // given
            String name = "duplicated";
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(name, null);
            given(categoryRepository.existsByNameAndParentCategory(name, null)).willReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).existsByNameAndParentCategory(name, null);
            verify(categoryRepository, times(0)).save(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 하위 카테고리를 추가하고 entity를 반환한다.")
        void Success_NewSubCategoryOfExistentCategory_ReturnEntity() {
            // given
            Category parentEntity = testParentCategoryEntity;
            Category entity = testChildCategoryEntity;
            Long parentId = parentEntity.getIdx();
            String name = entity.getName();
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(name, parentId);

            given(categoryRepository.findById(parentId)).willReturn(Optional.of(parentEntity));
            given(categoryRepository.existsByNameAndParentCategory(name, parentEntity)).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(entity);

            // when
            Category returnEntity = categoryService.createCategory(inputDto);

            // then
            verify(categoryRepository).findById(parentId);
            verify(categoryRepository).existsByNameAndParentCategory(name, parentEntity);
            verify(categoryRepository).save(any(Category.class));
            assertThat(returnEntity)
                .extracting("name", "parentCategory.idx")
                .containsExactly(name, parentId);
        }

        @Test
        @DisplayName("실패: 중복된 하위 카테고리 추가 시 예외를 던진다.")
        void Fail_DuplicatedSubCategoryOfOneCategory_ThrowException() {
            // given
            Category parentEntity = testParentCategoryEntity;
            Category entity = testChildCategoryEntity;
            Long parentId = parentEntity.getIdx();
            String name = entity.getName();
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(name, parentId);

            given(categoryRepository.findById(parentId)).willReturn(Optional.of(parentEntity));
            given(categoryRepository.existsByNameAndParentCategory(name, parentEntity)).willReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).findById(parentId);
            verify(categoryRepository).existsByNameAndParentCategory(name, parentEntity);
            verify(categoryRepository, times(0)).save(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리에 하위 카테고리 추가 시 예외를 던진다")
        void Fail_SubCategoryOfNonExistentCategory_ThrowException() {
            // given
            Long parentId = -1l;
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto("name", parentId);

            given(categoryRepository.findById(parentId)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).findById(parentId);
            verify(categoryRepository, times(0)).save(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("상위 카테고리"));
        }
    }

    @Nested
    @DisplayName("Read")
    class Test_Read_Category {
        @Test
        @DisplayName("성공: 모든 카테고리를 List로 반환한다.")
        void Success_AllCategory_ReturnEntityList() {
            // given - testParentCategoryEntity
            int size = 8;
            List<Category> foundList = new ArrayList<>();
            Category parentCategory = testParentCategoryEntity;
            foundList.add(parentCategory);
            for (Long i = 1L; i <= 5L; ++i) {
                Category category = EntityCreator.category(null, "topLevel" + 1, null);
                foundList.add(category);
            }
            for (Long i = 6L; i <= size; ++i) {
                Category category = EntityCreator.category(null, "child" + 1, parentCategory);
                foundList.add(category);
            }
            given(categoryRepository.findAll()).willReturn(foundList);

            // when
            List<Category> returnEntityList = categoryService.getAllCategories();

            // then
            verify(categoryRepository).findAll();
            assertThat(returnEntityList)
                .size()
                .isEqualTo(size + 1);
            assertThat(returnEntityList)
                .filteredOn(c -> c.getParentCategory() == null)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 카테고리별 포스트 개수와 함깨 entity list로 반환한다.")
        void Success_AllCategoriesWithPostsCount_ReturnEntityList() {
            // given
            Boolean visible = null;
            given(categoryRepository.allCategoriesWithPostsCount(eq(visible))).willReturn(List.of(
                new Category.WithPostsCount(testParentCategoryEntity, 3l),
                new Category.WithPostsCount(testChildCategoryEntity, 5l)
            ));

            // when
            List<Category.WithPostsCount> postsCountList = categoryService.getAllCategoriesWithPostsCount(visible);

            // then
            verify(categoryRepository).allCategoriesWithPostsCount(eq(visible));
            assertThat(postsCountList).size().isEqualTo(2);
            assertThat(postsCountList)
                .extracting("category", "postsCount")
                .containsExactly(
                    tuple(testParentCategoryEntity, 3l),
                    tuple(testChildCategoryEntity, 5l)
                );
        }

        @Test
        @DisplayName("성공: 카테고리별 visible 포스트 개수와 함께 entity list로 반환한다.")
        void Success_AllCategoriesWithVisiblePostsCount_ReturnEntityList() {
            // given
            Boolean visible = true;
            given(categoryRepository.allCategoriesWithPostsCount(eq(visible))).willReturn(List.of(
                new Category.WithPostsCount(testParentCategoryEntity, 3l),
                new Category.WithPostsCount(testChildCategoryEntity, 5l)
            ));

            // when
            List<Category.WithPostsCount> postsCountList = categoryService.getAllCategoriesWithPostsCount(visible);

            // then
            verify(categoryRepository).allCategoriesWithPostsCount(eq(visible));
            assertThat(postsCountList).size().isEqualTo(2);
            assertThat(postsCountList)
                .extracting("category", "postsCount")
                .containsExactly(
                    tuple(testParentCategoryEntity, 3l),
                    tuple(testChildCategoryEntity, 5l)
                );
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Category {
        @Test
        @DisplayName("카테고리 이름을 변경하고 entity를 반환한다.")
        void Success_CategoryName_ReturnEntity() {
            // given
            Category category = testParentCategoryEntity;
            Long categoryId = category.getIdx();
            String after = "after";

            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(after, null);

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
            given(categoryRepository.existsByNameAndParentCategory(after, null)).willReturn(false);

            // when
            Category updatedEntity = categoryService.updateCategory(categoryId, inputDto);

            // then
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).existsByNameAndParentCategory(after, null);
            assertThat(updatedEntity)
                .extracting("name")
                .isEqualTo(after);
        }

        @Test
        @DisplayName("성공: 부모 카테고리를 변경하고 entity를 반환한다.")
        void Success_ParentCategory_ReturnEntity() {
            // given
            Category entity = testChildCategoryEntity;
            Long categoryId = entity.getIdx();
            String name = entity.getName();

            Category parentAfterCategory = testParentCategoryEntity;
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(null, parentAfterCategory.getIdx());

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(entity));
            given(categoryRepository.findById(inputDto.getParentId())).willReturn(Optional.of(parentAfterCategory));
            given(categoryRepository.existsByNameAndParentCategory(name, parentAfterCategory)).willReturn(false);

            // when
            Category updatedEntity = categoryService.updateCategory(categoryId, inputDto);

            // then
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).findById(inputDto.getParentId());
            verify(categoryRepository).existsByNameAndParentCategory(name, parentAfterCategory);
            assertThat(updatedEntity)
                .extracting("name", "parentCategory.idx")
                .containsExactly(name, parentAfterCategory.getIdx());
        }

        @Test
        @DisplayName("성공: 부모 카테고리를 null로 변경하여 top-level로 만든다.")
        void Success_NullParentCategory_ReturnEntity() {
            // given
            Category entity = testChildCategoryEntity;
            Long categoryId = entity.getIdx();
            String name = entity.getName();
            CategoryDto.Req inputAfterDto = DtoCreator.categoryReqDto(null, null);

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(entity));
            given(categoryRepository.existsByNameAndParentCategory(name, null)).willReturn(false);

            // when
            Category updatedEntity = categoryService.updateCategory(categoryId, inputAfterDto);

            // then
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).existsByNameAndParentCategory(name, null);
            assertThat(updatedEntity)
                .extracting("name", "parentCategory")
                .containsExactly(name, null);
        }

        @Test
        @DisplayName("실패: 중복되는 카테고리로 변경 시 예외를 던진다.")
        void Fail_DuplicatedCategory_ThrowException() {
            // given - testParentCategoryEntity
            Category entity = testParentCategoryEntity;
            String name = "duplicatedName";

            CategoryDto.Req dto = DtoCreator.categoryReqDto(name, null);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(entity));
            given(categoryRepository.existsByNameAndParentCategory(name, null)).willReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> categoryService.updateCategory(anyLong(), dto));

            // then
            verify(categoryRepository).findById(anyLong());
            verify(categoryRepository).existsByNameAndParentCategory(name, null);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("실패: 존재 하지 않는 부모 카테고리로 변경 시 예외를 던진다.")
        void Fail_NonExistentParentCategory_ThrowException() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            Category entity = testChildCategoryEntity;
            Long invalidId = -1l;

            Long categoryId = entity.getIdx();
            CategoryDto.Req inputDto = DtoCreator.categoryReqDto(entity.getName(), invalidId);

            given(categoryRepository.findById(eq(categoryId))).willReturn(Optional.of(entity));
            given(categoryRepository.findById(eq(invalidId))).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "상위 카테고리")
            );

            // when
            Throwable thrown = catchThrowable(() -> categoryService.updateCategory(categoryId, inputDto));

            // then
            verify(categoryRepository).findById(eq(categoryId));
            verify(categoryRepository).findById(eq(invalidId));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("상위 카테고리"));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Test_Delete_Category {
        @Test
        @DisplayName("성공: 카테고리 하나를 삭제하고 true를 반환한다.")
        void Success_OneCategory_ReturnTrue() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(testChildCategoryEntity));

            // when
            boolean isDeleted = categoryService.deleteCategory(anyLong());

            // then
            verify(categoryRepository).findById(anyLong());
            verify(categoryRepository).delete(testChildCategoryEntity);
            assertThat(isDeleted)
                .isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리 삭제 시 예외를 던진다.")
        void Fail_NonExistentCategory_ThrowException() {
            // given
            given(categoryRepository.findById(anyLong())).willThrow(
                new ApiException(ErrorMessage.NON_EXISTENT, "카테고리")
            );

            // when
            Throwable thrown = catchThrowable(() -> categoryService.deleteCategory(anyLong()));

            // then
            verify(categoryRepository).findById(anyLong());
            verify(categoryRepository, times(0)).delete(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("실패: 포스트의 카테고리로 참조되는 카테고리 제거 시 예외를 던진다.")
        void Fail_CategoryOfPosts_ThrowException() {
            // given - testParentCategoryEntity
            Category category = testParentCategoryEntity;
            Post post = EntityCreator.post(null, category, "title", "content", "desc", true);
            post.updateInfo(category, null, null, null, null);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.deleteCategory(anyLong()));

            // then
            verify(categoryRepository).findById(anyLong());
            verify(categoryRepository, times(0)).delete(category);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NOT_REMOVEABLE_CATEGORY.getMessage());
        }
    }
}
