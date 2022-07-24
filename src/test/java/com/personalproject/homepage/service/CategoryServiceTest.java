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
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.helper.MockEntity;
import com.personalproject.homepage.mapper.CategoryMapper;
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
    private CategoryMapper categoryMapper;
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
        categoryMapper = new CategoryMapper(categoryRepository);
        categoryService = new CategoryService(categoryRepository, categoryMapper);

        testParentCategoryEntity = MockEntity.mock(Category.class, 99L);
        testParentCategoryEntity.updateInfo("testParent", null);
        testChildCategoryEntity = MockEntity.mock(Category.class, 100L);
        testChildCategoryEntity.updateInfo("testChild", testParentCategoryEntity);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Category {
        @Test
        @DisplayName("성공: 최상위 카테고리를 추가하고 dto를 반환한다.")
        void Success_NewTopLevelCategory_ReturnDto() {
            // given
            String name = "category";
            Category createdEntity = MockEntity.mock(Category.class, 1L);
            createdEntity.updateInfo(name, null);
            CategoryDto inputDto = CategoryDto.builder().name(name).build();
            given(categoryRepository.findByNameAndParentCategory(name, null)).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willReturn(createdEntity);

            // when
            CategoryDto returnDto = categoryService.createCategory(inputDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(name, null);
            verify(categoryRepository).save(any(Category.class));
            assertThat(returnDto)
                .extracting("name", "parent")
                .containsExactly(name, null);
        }

        @Test
        @DisplayName("실패: 카테고리 name이 null일 경우 예외를 던진다.")
        void Fail_NullNameCategory_ThrowException() {
            // given
            String name = null;
            CategoryDto inputDto = CategoryDto.builder().name(name).build();

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository, times(0)).delete(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NOT_ALLOWED_NULL.getMessage("name"));
        }

        @Test
        @DisplayName("실패: 중복된 최상위 카테고리 추가 시 예외를 던진다.")
        void Fail_DuplicatedTopLevelCategory_ThrowException() {
            // given - testParentCategoryEntity
            Category category = testParentCategoryEntity;
            String name = category.getName();
            CategoryDto inputDto = CategoryDto.builder().name(name).build();
            given(categoryRepository.findByNameAndParentCategory(name, null)).willReturn(Optional.of(category));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(name, null);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 하위 카테고리를 추가하고 dto를 반환한다.")
        void Success_NewSubCategoryOfExistentCategory_ReturnDto() {
            // given - testParentCategoryEntity
            Category parentCategory = testParentCategoryEntity;
            String parentName = parentCategory.getName();
            String childName = "child";
            CategoryDto inputDto = CategoryDto.builder().name(childName).parent(parentName).build();
            Category createdCategory = MockEntity.mock(Category.class, 1L);
            createdCategory.updateInfo(childName, parentCategory);

            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willReturn(createdCategory);

            // when
            CategoryDto returnDto = categoryService.createCategory(inputDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            verify(categoryRepository).save(any(Category.class));
            assertThat(returnDto)
                .extracting("name", "parent")
                .containsExactly(childName, parentName);
        }

        @Test
        @DisplayName("실패: 중복된 하위 카테고리 추가 시 예외를 던진다.")
        void Fail_DuplicatedSubCategoryOfOneCategory_ThrowException() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            Category parentCategory = testParentCategoryEntity;
            Category childCategory = testChildCategoryEntity;
            String parentName = parentCategory.getName();
            String childName = childCategory.getName();
            CategoryDto inputDto = CategoryDto.builder().name(childName).parent(parentName).build();
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.of(childCategory));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            verify(categoryRepository, times(0)).save(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리에 하위 카테고리 추가 시 예외를 던진다")
        void Fail_SubCategoryOfNonExistentCategory_ThrowException() {
            // given
            String parentName = "invalid";
            String childName = "child";
            CategoryDto inputDto = CategoryDto.builder().name(childName).parent(parentName).build();
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> categoryService.createCategory(inputDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
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
        void Success_AllCategory_ReturnDtoList() {
            // given - testParentCategoryEntity
            int size = 8;
            List<Category> foundList = new ArrayList<>();
            Category parentCategory = testParentCategoryEntity;
            foundList.add(parentCategory);
            for (Long i = 1L; i <= 5L; ++i) {
                Category category = MockEntity.mock(Category.class, i);
                category.updateInfo("topLevel" + i, null);
                foundList.add(category);
            }
            for (Long i = 6L; i <= size; ++i) {
                Category category = MockEntity.mock(Category.class, i);
                category.updateInfo("child" + i, parentCategory);
                foundList.add(category);
            }
            given(categoryRepository.findAll()).willReturn(foundList);

            // when
            List<CategoryDto> returnDtoList = categoryService.getAllCategories();

            // then
            verify(categoryRepository).findAll();
            assertThat(returnDtoList)
                .size()
                .isEqualTo(size + 1);
            assertThat(returnDtoList)
                .filteredOn(c -> c.getParent() == null)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 모든 최상위 카테고리를 dto List로 반환한다.")
        void Success_AllTopLevelCategory_ReturnDtoList() {
            // given
            int size = 5;
            List<Category> foundTopLevelList = new ArrayList<>();
            for (Long i = 1L; i <= size; ++i) {
                Category category = MockEntity.mock(Category.class, i);
                category.updateInfo("parent" + i, null);
                foundTopLevelList.add(category);
            }
            given(categoryRepository.findAllByParentCategoryIsNull()).willReturn(foundTopLevelList);

            // when
            List<CategoryDto> returnDtoList = categoryService.getAllTopLevelCategories();

            // then
            verify(categoryRepository).findAllByParentCategoryIsNull();
            assertThat(returnDtoList)
                .allMatch(c -> c.getParent() == null)
                .size()
                .isEqualTo(size);
        }

        @Test
        @DisplayName("성공: 카테고리에 속한 모든 하위 카테고리를 dto List로 반환한다.")
        void Success_AllSubCategoryOfOneCategory_ReturnDtoList() {
            // given - total 6 categories including testChildCategoryEntity inside testParentCategoryEntity
            Category parentCategory = testParentCategoryEntity;
            String parentName = parentCategory.getName();
            int size = 5;
            for (Long i = 1L; i <= size; ++i) {
                Category category = MockEntity.mock(Category.class, i + 1);
                category.updateInfo("child" + i, parentCategory);
            }
            CategoryDto inputDto = CategoryDto.builder().name(parentName).build();
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));

            // when
            List<CategoryDto> returnDtoList = categoryService.getAllSubCategoriesOf(inputDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            assertThat(returnDtoList)
                .allMatch(c -> c.getParent().equals(parentName))
                .size()
                .isEqualTo(size + 1);
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Category {
        @Test
        @DisplayName("카테고리 이름을 변경하고 dto를 반환한다.")
        void Success_CategoryName_ReturnDto() {
            //given - testParentCategoryEntity
            Category category = testParentCategoryEntity;
            String before = category.getName();
            String after = "after";

            CategoryDto beforeDto = CategoryDto.builder().name(before).build();
            CategoryDto afterDto = CategoryDto.builder().name(after).build();
            given(categoryRepository.findByNameAndParentCategory(before, null)).willReturn(Optional.of(category));
            given(categoryRepository.findByNameAndParentCategory(after, null)).willReturn(Optional.empty());

            // when
            CategoryDto updatedDto = categoryService.updateCategory(beforeDto, afterDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(before, null);
            verify(categoryRepository).findByNameAndParentCategory(after, null);
            assertThat(updatedDto)
                .extracting("name")
                .isEqualTo(after);
        }

        @Test
        @DisplayName("성공: 부모 카테고리를 변경하고 dto를 반환한다.")
        void Success_ParentCategory_ReturnDto() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            Category parentBeforeCategory = testParentCategoryEntity;
            String parentBefore = parentBeforeCategory.getName();
            String parentAfter = "parentAfter";
            Category parentAfterCategory = MockEntity.mock(Category.class, 2L);
            parentAfterCategory.updateInfo(parentAfter, null);
            Category childCategory = testChildCategoryEntity;
            String child = childCategory.getName();

            CategoryDto inputBeforeDto = CategoryDto.builder().name(child).parent(parentBefore).build();
            CategoryDto inputAfterDto = CategoryDto.builder().name(child).parent(parentAfter).build();
            given(categoryRepository.findByNameAndParentCategory(parentBefore, null)).willReturn(Optional.of(parentBeforeCategory));
            given(categoryRepository.findByNameAndParentCategory(child, parentBeforeCategory)).willReturn(Optional.of(childCategory));
            given(categoryRepository.findByNameAndParentCategory(parentAfter, null)).willReturn(Optional.of(parentAfterCategory));
            given(categoryRepository.findByNameAndParentCategory(child, parentAfterCategory)).willReturn(Optional.empty());

            // when
            CategoryDto updatedDto = categoryService.updateCategory(inputBeforeDto, inputAfterDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentBefore, null);
            verify(categoryRepository).findByNameAndParentCategory(child, parentBeforeCategory);
            verify(categoryRepository).findByNameAndParentCategory(parentAfter, null);
            verify(categoryRepository).findByNameAndParentCategory(child, parentAfterCategory);
            assertThat(updatedDto)
                .extracting("name", "parent")
                .containsExactly(child, parentAfter);
        }

        @Test
        @DisplayName("성공: 부모 카테고리를 null로 변경하여 top-level로 만든다.")
        void Success_NullParentCategory_ReturnDto() {
            // given - testChildCategoryEntity
            Category childCategory = testChildCategoryEntity;
            String childName = childCategory.getName();
            Category parentCategory = childCategory.getParentCategory();
            String parentName = parentCategory.getName();
            CategoryDto inputBeforeDto = CategoryDto.builder().name(childName).parent(parentName).build();
            CategoryDto inputAfterDto = CategoryDto.builder().parent(null).build();

            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.of(childCategory));

            // when
            CategoryDto updatedDto = categoryService.updateCategory(inputBeforeDto, inputAfterDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            assertThat(updatedDto)
                .extracting("name", "parent")
                .containsExactly(childName, null);
        }

        @Test
        @DisplayName("실패: 포스트가 있는 카테고리의 부모 카테고리를 null로 변경 시 예외를 던진다.")
        void Fail_NullParentCategoryHavingPosts_ThrowException() {
            // given - testChildCategoryEntity
            Category childCategory = testChildCategoryEntity;
            String childName = childCategory.getName();
            Category parentCategory = childCategory.getParentCategory();
            String parentName = parentCategory.getName();
            CategoryDto inputBeforeDto = CategoryDto.builder().name(childName).parent(parentName).build();
            CategoryDto inputAfterDto = CategoryDto.builder().parent(null).build();

            childCategory.getPostsOfCategory().add(MockEntity.mock(Post.class));
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.of(childCategory));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.updateCategory(inputBeforeDto, inputAfterDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NOT_CHANGE_TO_TOPLEVEL_CATEGORY.getMessage());
        }

        @Test
        @DisplayName("실패: 중복되는 카테고리로 변경 시 예외를 던진다.")
        void Fail_DuplicatedCategory_ThrowException() {
            // given - testParentCategoryEntity
            String name = "category";
            Category category = MockEntity.mock(Category.class, 1L);
            category.updateInfo(name, null);
            Category existentCategory = testParentCategoryEntity;
            String duplicatedName = existentCategory.getName();

            CategoryDto inputBeforeDto = CategoryDto.builder().name(name).build();
            CategoryDto inputAfterDto = CategoryDto.builder().name(duplicatedName).build();
            given(categoryRepository.findByNameAndParentCategory(name, null)).willReturn(Optional.of(category));
            given(categoryRepository.findByNameAndParentCategory(duplicatedName, null)).willReturn(Optional.of(existentCategory));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.updateCategory(inputBeforeDto, inputAfterDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(name, null);
            verify(categoryRepository).findByNameAndParentCategory(duplicatedName, null);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.ALREADY_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("실패: 존재 하지 않는 부모 카테고리로 변경 시 예외를 던진다.")
        void Fail_NonExistentParentCategory_ThrowException() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            Category parentCategory = testParentCategoryEntity;
            String parentName = parentCategory.getName();
            Category childCategory = testChildCategoryEntity;
            String childName = childCategory.getName();
            String invalidParent = "invalid";

            CategoryDto inputBeforeDto = CategoryDto.builder().name(childName).parent(parentName).build();
            CategoryDto inputAfterDto = CategoryDto.builder().name(childName).parent(invalidParent).build();

            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.of(childCategory));
            given(categoryRepository.findByNameAndParentCategory(invalidParent, null)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> categoryService.updateCategory(inputBeforeDto, inputAfterDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            verify(categoryRepository).findByNameAndParentCategory(invalidParent, null);
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
            Category parentCategory = testParentCategoryEntity;
            String parentName = parentCategory.getName();
            Category childCategory = testChildCategoryEntity;
            String childName = parentCategory.getName();
            CategoryDto inputDto = CategoryDto.builder().name(parentName).parent(childName).build();
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndParentCategory(childName, parentCategory)).willReturn(Optional.of(childCategory));

            // when
            boolean isDeleted = categoryService.deleteCategory(inputDto);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).findByNameAndParentCategory(childName, parentCategory);
            verify(categoryRepository).delete(childCategory);
            assertThat(isDeleted)
                .isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리 삭제 시 예외를 던진다.")
        void Fail_NonExistentCategory_ThrowException() {
            // given
            String name = "invalid";
            CategoryDto inputDto = CategoryDto.builder().name(name).build();
            given(categoryRepository.findByNameAndParentCategory(name, null)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> categoryService.deleteCategory(inputDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(name, null);
            verify(categoryRepository, times(0)).delete(any(Category.class));
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NON_EXISTENT.getMessage("카테고리"));
        }

        @Test
        @DisplayName("성공: 다른 카테고리의 부모 카테고리인 것을 삭제하고 true를 반환한다.")
        void Success_ParentCategoryOfOtherCategory_ReturnTrue() {
            // given - testParentCategoryEntity, testChildCategoryEntity
            Category parentCategory = testParentCategoryEntity;
            String parentName = parentCategory.getName();
            CategoryDto inputCategory = CategoryDto.builder().name(parentName).build();
            given(categoryRepository.findByNameAndParentCategory(parentName, null)).willReturn(Optional.of(parentCategory));

            // when
            boolean isDeleted = categoryService.deleteCategory(inputCategory);

            // then
            verify(categoryRepository).findByNameAndParentCategory(parentName, null);
            verify(categoryRepository).delete(parentCategory);
            assertThat(isDeleted)
                .isTrue();
        }

        @Test
        @DisplayName("실패: 포스트의 카테고리로 참조되는 카테고리 제거 시 예외를 던진다.")
        void Fail_CategoryOfPosts_ThrowException() {
            // given - testParentCategoryEntity
            Category category = testParentCategoryEntity;
            String name = category.getName();
            Post post = MockEntity.mock(Post.class, 1L);
            post.updateInfo(category, "title", "content", "desc", true);
            CategoryDto inputDto = CategoryDto.builder().name(name).build();
            given(categoryRepository.findByNameAndParentCategory(name, null)).willReturn(Optional.of(category));

            // when
            Throwable thrown = catchThrowable(() -> categoryService.deleteCategory(inputDto));

            // then
            verify(categoryRepository).findByNameAndParentCategory(name, null);
            verify(categoryRepository, times(0)).delete(category);
            assertThat(thrown)
                .isInstanceOf(Exception.class)
                .hasMessage(ErrorMessage.NOT_REMOVEABLE_CATEGORY.getMessage());
        }
    }
}
