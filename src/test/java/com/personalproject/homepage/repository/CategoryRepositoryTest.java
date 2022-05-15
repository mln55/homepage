package com.personalproject.homepage.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.personalproject.homepage.entity.Category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class CategoryRepositoryTest {

    private Category savedParentCategory1;
    private Category savedParentCategory2;
    private Category savedChildCategory;

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryRepositoryTest(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    /********************************************************************************
                    CategoryRepository의 상황에 따른 동작을 테스트 한다.

    {@link #resetPersist}
        @BeforeEach test db에 카테고리를 insert한다.

    Create
        {@link Test_Create_Category#Success_TopLevelCategory_Create}
            - 최상위 카테고리를 추가한다.
        {@link Test_Create_Category#Success_DuplicatedTopLevelCategory_Create}
            - 중복된 최상위 카테고리를 추가한다. **주의**
        {@link Test_Create_Category#Success_SubCategoryOfExistentCategory_Create}
            - 하위 카테고리를 추가한다.
        {@link Test_Create_Category#Fail_SubCategoryOfNonExistentCategory_ThrowException}
            - 존재하지 않는 카테고리에 하위 카테고리 추가 시 예외를 던진다
        {@link Test_Create_Category#Fail_DuplicatedSubCategoryOfOneCategory_ThrowException}
            - 중복된 하위 카테고리 추가 시 예외를 던진다

    Read
        {@link Test_Read_Category#Success_OneCategory_ReturnCategoryOptional}
            - 카테고리 하나를 Optional<Category>로 반환한다.
        {@link Test_Read_Category#Success_WheterCategoryExist_ReturnBoolean}
            - 카테고리 존재 여부를 boolean으로 반환한다.
        {@link Test_Read_Category#Success_AllCategory_ReturnCategoryList}
            - 모든 카테고리를 List<Category>로 반환한다.
        {@link Test_Read_Category#Success_AllTopLevelCategory_ReturnCategoryList}
            - 모든 최상위 카테고리를 List<Category>로 반환한다.
        {@link Test_Read_Category#Success_AllSubCategoryOfOneCategory_ReturnCategoryList}
            - 카테고리에 속한 모든 하위 카테고리를 List<Category>로 반환한다.
    Update
        {@link Test_Update_Category#Success_CategoryName_Update}
            - 카테고리 이름을 변경한다.
        {@link Test_Update_Category#Success_CategoryNameReferencedAsOthers_CascadeUpdate}
            - 다른 곳에 참조되는 카테고리의 이름을 변경한다.

    Delete
        {@link Test_Delete_Category#Success_OneCategory_Delete}
            - 카테고리 하나를 삭제한다.
        {@link Test_Delete_Category#Success_CategoryReferencedAsOthers_CascadeDelete}
            - 다른 곳에 참조되는 카테고리를 삭제한다.
    ********************************************************************************/

    @BeforeEach
    void resetPersist() {
        savedParentCategory1 = Category.builder()
            .name("savedParent1")
            .build();
        savedParentCategory2 = Category.builder()
            .name("savedParent2")
            .build();
        categoryRepository.save(savedParentCategory1);
        categoryRepository.save(savedParentCategory2);

        savedChildCategory = Category.builder()
            .name("savedChild")
            .parentCategory(savedParentCategory1)
            .build();
        categoryRepository.save(savedChildCategory);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Category {
        @Test
        @DisplayName("성공: 최상위 카테고리를 추가한다.")
        void Success_TopLevelCategory_Create() {
            // given
            Category category = Category.builder()
                .name("category")
                .build();

            // when
            categoryRepository.save(category);

            // then
            assertThat(category)
                .extracting("categoryIdx")
                .isNotNull();
        }

        @Test
        @DisplayName("성공: 중복된 최상위 카테고리를 추가한다.")
        void Success_DuplicatedTopLevelCategory_Create() {
            /********************************************************************************
                                            **주의**
                        테이블 UNIQUE 설정이 (name, parent_category_idx)로 되어있다.
                        parent_category_idx가 null인 중복된 name이 들어갈 수 있으므로
                        service 객체에서 반드시 검증을 거쳐야 한다.
            ********************************************************************************/
            // given - savedParentCategory1
            Category duplicatedCategory = Category.builder()
                .name(savedParentCategory1.getName())
                .build();

            // when
            categoryRepository.save(duplicatedCategory);

            // then
            assertThat(duplicatedCategory)
                .extracting("name")
                .isEqualTo(savedParentCategory1.getName());
            assertThat(duplicatedCategory)
                .extracting("categoryIdx")
                .isNotEqualTo(savedParentCategory1.getCategoryIdx());
        }

        @Test
        @DisplayName("성공: 하위 카테고리를 추가한다.")
        void Success_SubCategoryOfExistentCategory_Create() {
            // given - savedParentCategory1
            Category childCategory = Category.builder()
                .name("child")
                .parentCategory(savedParentCategory1)
                .build();

            // when
            categoryRepository.save(childCategory);

            // then
            assertThat(childCategory)
                .extracting("parentCategory.name")
                .isEqualTo(savedParentCategory1.getName());
            }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리에 하위 카테고리 추가 - throw Exception")
        void Fail_SubCategoryOfNonExistentCategory_ThrowException() {
            // given
            Category parentCategory = Category.builder()
                .name("parent")
                .build();
            parentCategory.setCategoryIdx(0L); // 강제로 idx 입력

            Category childCategory = Category.builder()
                .name("child")
                .parentCategory(parentCategory)
                .build();

            // when
            Throwable thrown = catchThrowable(() -> categoryRepository.save(childCategory));

            // then
            assertThat(thrown)
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("실패: 중복된 하위 카테고리 추가 - throw Exception")
        void Fail_DuplicatedSubCategoryOfOneCategory_ThrowException() {
            // given - savedParentCategory1
            Category duplicatedChildCategory = Category.builder()
                .name("savedChild")
                .parentCategory(savedParentCategory1)
                .build();

            // when
            Throwable thrown = catchThrowable(() -> categoryRepository.save(duplicatedChildCategory));

            // then
            assertThat(thrown)
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Read")
    class Test_Read_Category {
        @Test
        @DisplayName("성공: 카테고리 명으로 카테고리 Optional을 반환한다.")
        void Success_OneCategory_ReturnCategoryOptional() {
            // given - savedChildCategory
            String name = "savedChild";

            // when
            Optional<Category> savedCategory = categoryRepository.findByName(name);

            // then
            assertThat(savedCategory)
                .isPresent();
        }

        @Test
        @DisplayName("성공: 카테고리가 있는 지를 boolean으로 반환한다.")
        void Success_WheterCategoryExist_ReturnBoolean() {
            // given - savedChildCategory
            String name = "savedChild";

            // when
            boolean isPresent = categoryRepository.existsByName(name);

            // then
            assertThat(isPresent)
                .isTrue();
        }

        @Test
        @DisplayName("성공: 모든 카테고리를 List로 반환한다.")
        void Success_AllCategory_ReturnCategoryList() {
            // given - resetPersist()

            // when
            List<Category> categoryList = categoryRepository.findAll();

            // then
            assertThat(categoryList)
                .size()
                .isEqualTo(3);
        }

        @Test
        @DisplayName("성공: 모든 최상위 카테고리를 List로 반환한다.")
        void Success_AllTopLevelCategory_ReturnCategoryList() {
            // given - parent1 and parent2 at resetPersist()

            // when
            List<Category> categoryList = categoryRepository.findAllByParentCategoryIsNull();

            // then
            assertThat(categoryList)
                .size()
                .isEqualTo(2);
            assertThat(categoryList)
                .flatMap(Category::getParentCategory)
                .containsExactly(null, null);
        }

        @Test
        @DisplayName("성공: 카테고리의 모든 하위 카테고리를 List로 반환한다.")
        void Success_AllSubCategoryOfOneCategory_ReturnCategoryList() {
            // given - child of parent1 at resetPersist()
            Category childCategory = Category.builder()
                .name("newSavedChild")
                .parentCategory(savedParentCategory1)
                .build();
            categoryRepository.save(childCategory);

            // when
            List<Category> categoryList = categoryRepository.findAllByParentCategory(savedParentCategory1);

            // then
            assertThat(categoryList)
                .size()
                .isEqualTo(2);
            assertThat(categoryList)
                .flatMap(Category::getName)
                .containsExactly("savedChild", "newSavedChild");
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Category {
        @Test
        @DisplayName("성공: 카테고리 이름을 변경한다.")
        void Success_CategoryName_Update() {
            // given - savedParentCategory1

            // then
            savedParentCategory1.setName("changed");
            categoryRepository.save(savedParentCategory1);

            boolean isPresent = categoryRepository.existsByName("changed");
            assertThat(isPresent)
                .isTrue();
        }

        @Test
        @DisplayName("성공: 다른 곳에 참조되는 카테고리의 이름을 변경한다.")
        void Success_CategoryNameReferencedAsOthers_CascadeUpdate() {
            // given - savedParentCategory1

            // when
            savedParentCategory1.setName("changed");
            categoryRepository.save(savedParentCategory1);

            Category updatedParentCategory = categoryRepository.findByName("changed").orElseThrow();
            Category updatedChildCategory = categoryRepository.findByName("savedChild").orElseThrow();

            // then
            assertThat(updatedParentCategory)
                .extracting("name")
                .isEqualTo("changed");
            assertThat(updatedChildCategory)
                .extracting("parentCategory.name")
                .isEqualTo("changed");
        }
    }

    @Nested
    @DisplayName("Delete")
    class Test_Delete_Category {
        @Test
        @DisplayName("성공: 카테고리 하나를 삭제한다.")
        void Success_OneCategory_Delete() {
            // given - savedParentCategory2

            // when
            categoryRepository.delete(savedParentCategory2);
            boolean isPresent = categoryRepository.existsByName("savedParent2");

            // then
            assertThat(isPresent)
                .isFalse();

        }
        @Test
        @DisplayName("성공: 다른 곳에 참조되는 카테고리를 삭제한다.")
        void Success_CategoryReferencedAsOthers_CascadeDelete() {
            // given - savedParentCategory1

            // when
            categoryRepository.delete(savedParentCategory1);
            boolean isPresentParent = categoryRepository.existsByName("savedParent1");
            boolean isPresentChild = categoryRepository.existsByName("savedChild");

            // then
            assertThat(isPresentParent)
                .isFalse();
            assertThat(isPresentChild)
                .isFalse();
        }
    }
}
