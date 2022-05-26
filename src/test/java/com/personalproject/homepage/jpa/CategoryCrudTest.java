package com.personalproject.homepage.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.helper.MockEntity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/********************************************************************************
    @DataJpaTest - JPA 관련 components들만 scan
    @AutoConfigureTestDatabase - embeded db가 아닌 test용 외부 h2 db 사용을 위해 설정
    @ActiveProfiles - test용 application 설정을 위함. application.yml 설정으로 대체 가능
********************************************************************************/
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class CategoryCrudTest {

    private final TestEntityManager tem;

    @Autowired
    public CategoryCrudTest(TestEntityManager testEntityManager) {
        this.tem = testEntityManager;
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Category {
        /********************************************************************************
            ID컬럼의 @GeneratedValue Strategy가 IDENTITY이므로 tem을 flush하지 않는다.
        *********************************************************************************/
        @Test
        @DisplayName("성공: 최상위 카테고리 추가 - 부모 카테고리 없음")
        void Success_TopLevelCategory_Create() {
            // given
            String c = "category";
            Category category = MockEntity.mock(Category.class);
            category.updateInfo(c, null);

            // when
            Category savedCategory = tem.persist(category);

            // then
            assertThat(savedCategory)
                .extracting("name", "parentCategory")
                .containsExactly(c, null);
            assertThat(savedCategory)
                .extracting("createAt")
                .isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("성공: 하위 카테고리 추가 - 부모 카테고리 있음")
        void Success_SubCategory_Create() {
            // given
            String parent = "parent";
            Category parentCategory = MockEntity.mock(Category.class);
            parentCategory.updateInfo(parent, null);
            tem.persist(parentCategory);
            tem.clear();

            String child = "child";
            Category childCategory = MockEntity.mock(Category.class);
            childCategory.updateInfo(child, parentCategory);

            // when
            Category savedCategory = tem.persist(childCategory);

            // then
            assertThat(savedCategory)
                .extracting("name", "parentCategory.name")
                .containsExactly(child, parent);
            assertThat(savedCategory)
                .extracting("createAt")
                .isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("실패: 중복된 하위 카테고리 추가")
        void Fail_DulpicatedSubCategory_ThrowException() {
            // given
            String parent = "parent";
            Category parentCategory = MockEntity.mock(Category.class);
            parentCategory.updateInfo(parent, null);
            tem.persist(parentCategory);

            String child = "child";
            Category subCategory = MockEntity.mock(Category.class);
            subCategory.updateInfo(child, parentCategory);
            tem.persist(subCategory);

            Category savedParentCategory = tem.find(Category.class, parentCategory.getIdx());
            Category duplicatedCategory = MockEntity.mock(Category.class);
            duplicatedCategory.updateInfo(child, savedParentCategory);

            // when
            Throwable thrown = catchThrowable(() -> tem.persist(duplicatedCategory));

            // then
            assertThat(thrown)
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Read - SKIP")
    class Test_Read_Category {
        /********************************************************************************
            category, parentCategory가 기본키가 아니다.
            repository로 find시 category 및 parentCategory(자기 참조 외래키)로 쿼리가 생성 돼,
            JPA 테스트 시 직접 쿼리를 생성해야 하기에 스킵한다.
        ********************************************************************************/
        @Test
        @Disabled
        @DisplayName("SKIP")
        void Test_Read_Skip() { }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Category {
        @Test
        @DisplayName("성공: 카테고리 명을 변경한다.")
        void Success_Name_Update() {
            // given
            String before = "before";
            String after = "after";
            Category category = MockEntity.mock(Category.class);
            category.updateInfo(before, null);
            tem.persist(category);
            tem.clear();

            // when
            Category targetCategory = tem.find(Category.class, category.getIdx());
            targetCategory.updateInfo(after, null);
            tem.merge(targetCategory);
            tem.flush();
            tem.clear();
            Category updatedCategory = tem.find(Category.class, category.getIdx());

            // then
            assertThat(updatedCategory)
                .extracting("name")
                .isEqualTo(after);
            assertThat(updatedCategory)
                .extracting("updateAt")
                .isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("성공: parentCategory를 변경한다.")
        void Success_ParentCategory_Update() {
            // given
            String parent1 = "parent1";
            String parent2 = "parent2";
            String child = "child";

            Category parentCategory1 = MockEntity.mock(Category.class);
            parentCategory1.updateInfo(parent1, null);
            Category parentCategory2 = MockEntity.mock(Category.class);
            parentCategory2.updateInfo(parent2, null);
            tem.persist(parentCategory1);
            tem.persist(parentCategory2);
            tem.clear();

            Category childCategory = MockEntity.mock(Category.class);
            childCategory.updateInfo(child, parentCategory1);
            tem.persist(childCategory);
            tem.clear();

            // when
            Category targetChildCategory = tem.find(Category.class, childCategory.getIdx());
            targetChildCategory.updateInfo(null, parentCategory2);
            tem.merge(targetChildCategory);
            tem.flush();
            tem.clear();
            Category updatedCategory = tem.find(Category.class, childCategory.getIdx());

            // then
            assertThat(updatedCategory)
                .extracting("name", "parentCategory.name", "parentCategory.updateAt")
                .containsExactly(child, parent2, null);
            assertThat(updatedCategory)
                .extracting("updateAt")
                .isInstanceOf(LocalDateTime.class);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Test_Delete_Category {
        @Test
        @DisplayName("성공: 최상위 카테고리를 삭제한다.")
        void Success_TopLevelCategory_Delete() {
            // given
            Category category = MockEntity.mock(Category.class);
            category.updateInfo("category", null);
            tem.persist(category);
            tem.clear();

            // when
            Throwable notThrown = catchThrowable(() -> {
                tem.remove(tem.find(Category.class, category.getIdx()));
                tem.flush();
                tem.clear();
            });

            // then
            assertThat(notThrown)
                .isNull();
        }

        @Test
        @DisplayName("성공: 하위 카테고리를 삭제한다.")
        void Success_SubCategory_delete() {
            // given
            Category parentCategory = MockEntity.mock(Category.class);
            parentCategory.updateInfo("parent", null);
            tem.persist(parentCategory);
            tem.clear();

            Category childCategory = MockEntity.mock(Category.class);
            childCategory.updateInfo("child", tem.find(Category.class, parentCategory.getIdx()));
            tem.persist(childCategory);
            tem.clear();

            // when
            Throwable notThrown = catchThrowable(() -> {
                tem.remove(tem.find(Category.class, childCategory.getIdx()));
                tem.flush();
                tem.clear();
            });

            // then
            assertThat(notThrown)
                .isNull();
        }

        @Test
        @DisplayName("성공: parentCategory로 참조되는 category를 삭제한다.")
        void Success_CategoryReferencedToOthers_CascadeDelete() {
            // given
            Category parentCategory = MockEntity.mock(Category.class);
            parentCategory.updateInfo("parent", null);
            tem.persist(parentCategory);
            tem.clear();

            Category childCategory = MockEntity.mock(Category.class);
            childCategory.updateInfo("child", tem.find(Category.class, parentCategory.getIdx()));
            tem.persist(childCategory);
            tem.clear();

            // when
            Throwable notThrown = catchThrowable(() -> {
                tem.remove(tem.find(Category.class, parentCategory.getIdx()));
                tem.flush();
                tem.clear();
            });

            // then
            assertThat(notThrown)
                .isNull();
        }
    }
}
