package com.personalproject.homepage.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.helper.MockEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class PostCrudTest {

    private Post post;
    private final TestEntityManager tem;

    @Autowired
    public PostCrudTest(TestEntityManager testEntityManager) {
        this.tem = testEntityManager;
    }

    // not null 컬럼을 채운 기본 post 객체 생성
    @BeforeEach
    void resetPost() {
        this.post = MockEntity.mock(Post.class);
        post.updateInfo(null, "title", "content", true);
    }

    @Nested
    @DisplayName("Create With Read")
    class Test_CreateWithRead_Post {
        @Test
        @DisplayName("성공: 포스트 추가 - 카테고리 null")
        void Success_PostWithNoCategory_Create() {
            // given
            tem.persist(post);
            tem.clear();

            // when
            Post savedPost = tem.find(Post.class, post.getIdx());

            // then
            assertThat(savedPost)
                .extracting("category", "hit", "updateAt")
                .containsExactly(null, 0L, null);
            assertThat(savedPost)
                .extracting("createAt")
                .isInstanceOf(LocalDateTime.class);
        }
        @Test
        @DisplayName("성공: 포스트 추가 - 카테고리 있음")
        void Success_PostWithCategory_Create() {
            // given
            Category category = MockEntity.mock(Category.class);
            category.updateInfo("category", null);
            tem.persist(category);
            post.updateInfo(category, null, null, null);
            tem.persist(post);
            tem.clear();

            // when
            Post savedPost = tem.find(Post.class, post.getIdx());

            // then
            assertThat(savedPost)
                .extracting("category.name", "hit", "updateAt")
                .containsExactly(category.getName(), 0L, null);
            assertThat(savedPost)
                .extracting("createAt")
                .isInstanceOf(LocalDateTime.class);
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Post {
        @Test
        @DisplayName("성공: 포스트 내용 변경")
        void Success_PostDetail_Update() {
            // given
            String titleChanged = "changed";
            tem.persist(post);
            tem.clear();

            // when
            Post targetPost = tem.find(Post.class, post.getIdx());
            targetPost.updateInfo(null, titleChanged, null, false);

            tem.merge(targetPost);
            tem.flush();
            tem.clear();

            Post updatedPost = tem.find(Post.class, post.getIdx());

            // then
            assertThat(updatedPost)
                .extracting("title", "visible")
                .containsExactly(titleChanged, false);
            assertThat(updatedPost)
                .extracting("updateAt")
                .isNotNull()
                .isInstanceOf(LocalDateTime.class);
        }
    }
    @Nested
    @DisplayName("Delete")
    class Test_Delete_Post {
        @Test
        @DisplayName("성공: 포스트 삭제")
        void Success_OnePost_Delete() {
            // given
            tem.persist(post);
            tem.clear();

            // when
            tem.remove(tem.find(Post.class, post.getIdx()));
            tem.flush();
            tem.clear();
            Throwable thrown = catchThrowable(() -> tem.find(Post.class, post.getIdx()));

            // then
            assertThat(thrown)
                .isNull();
        }
    }
}
