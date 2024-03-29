package com.personalproject.homepage.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.helper.EntityCreator;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class CategoryRepositoryTest {

    private Category savedParentCategory1;
    private Category savedParentCategory2;
    private Category savedChildCategory;

    private final CategoryRepository categoryRepository;

    private final PostRepository postRepository;

    @Autowired
    public CategoryRepositoryTest(CategoryRepository categoryRepository, PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
    }

    @BeforeEach
    void resetPersist() {
        savedParentCategory1 = EntityCreator.category(null, "savedParent1", null);
        savedParentCategory2 = EntityCreator.category(null, "savedParent2", null);
        categoryRepository.save(savedParentCategory1);
        categoryRepository.save(savedParentCategory2);

        savedChildCategory = EntityCreator.category(null, "savedChild", savedParentCategory1);
        categoryRepository.save(savedChildCategory);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Category {
        @Test
        @DisplayName("성공: 최상위 카테고리를 추가한다.")
        void Success_TopLevelCategory_Create() {
            // given
            Category category = EntityCreator.category(null, "category", null);

            // when
            categoryRepository.save(category);

            // then
            assertThat(category)
                .extracting("idx")
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
            Category duplicatedCategory = EntityCreator.category(null, savedParentCategory1.getName(), null);

            // when
            categoryRepository.save(duplicatedCategory);

            // then
            assertThat(duplicatedCategory)
                .extracting("name")
                .isEqualTo(savedParentCategory1.getName());
            assertThat(duplicatedCategory)
                .extracting("idx")
                .isNotEqualTo(savedParentCategory1.getIdx());
        }

        @Test
        @DisplayName("성공: 하위 카테고리를 추가한다.")
        void Success_SubCategoryOfExistentCategory_Create() {
            // given - savedParentCategory1
            Category childCategory = EntityCreator.category(null, "child", savedParentCategory1);

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
            Category parentCategory = EntityCreator.category(0l, "parent", null); // 존재하지 않는 카테고리

            Category childCategory = EntityCreator.category(null, "child", parentCategory);

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
            Category duplicatedChildCategory = EntityCreator.category(null, "savedChild", savedParentCategory1);

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
            // given - savedParentCategory1, savedChildCategory
            String name = "savedChild";

            // when
            Optional<Category> savedCategory = categoryRepository.findByNameAndParentCategory(name, savedParentCategory1);

            // then
            assertThat(savedCategory)
                .isPresent();
        }

        @Test
        @DisplayName("성공: 카테고리가 있는 지를 boolean으로 반환한다.")
        void Success_WheterCategoryExist_ReturnBoolean() {
            // given - savedParentCategory1, savedChildCategory
            String name = "savedChild";

            // when
            boolean isPresent = categoryRepository.existsByNameAndParentCategory(name, savedParentCategory1);

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
        @DisplayName("성공: 카테고리를 포스트 개수와 함께 반환한다.")
        void Success_CategoryWithPostsCount_ReturnObjectList() {
            // given
            IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    Post p = EntityCreator.post(null, i % 2 == 0 ? savedChildCategory : savedParentCategory1, "title", "content", "desc", true);
                    postRepository.save(p);
                });

            // when
            List<Category.WithPostsCount> categoryPostsCountList = categoryRepository.allCategoriesWithPostsCount(null);

            // then
            assertThat(categoryPostsCountList).size().isEqualTo(3);
            assertThat(categoryPostsCountList)
                .extracting("category.idx", "category.name")
                .doesNotHaveDuplicates();
            assertThat(categoryPostsCountList)
                .extracting("postsCount")
                .containsExactly(5l, 0l, 5l);
        }

        @Test
        @DisplayName("성공: 카테고리를 visible 포스트 개수와 함께 반환한다.")
        void Success_CategoryWithVisiblePostsCount_ReturnObjectList() {
            // given
            IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    Post p = EntityCreator.post(null, i % 2 == 0 ? savedChildCategory : savedParentCategory1, "title", "content", "desc", true);
                    postRepository.save(p);
                });

            // when
            List<Category.WithPostsCount> categoryPostsCountList = categoryRepository.allCategoriesWithPostsCount(true);

            // then
            assertThat(categoryPostsCountList).size().isEqualTo(3);
            assertThat(categoryPostsCountList)
                .extracting("category.idx", "category.name")
                .doesNotHaveDuplicates();
            assertThat(categoryPostsCountList)
                .extracting("postsCount")
                .containsExactly(5l, 0l, 5l);
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
            savedParentCategory1.updateInfo("changed", null);

            boolean isPresent = categoryRepository.existsByNameAndParentCategory("changed", null);
            assertThat(isPresent)
                .isTrue();
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
            boolean isPresent = categoryRepository.existsByNameAndParentCategory("savedParent2", null);

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
            boolean isPresentParent = categoryRepository.existsByNameAndParentCategory("savedParent1", null);
            boolean isPresentChild = categoryRepository.existsByNameAndParentCategory("savedChild", savedParentCategory1);

            // then
            assertThat(isPresentParent)
                .isFalse();
            assertThat(isPresentChild)
                .isFalse();
        }
    }
}
