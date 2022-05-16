package com.personalproject.homepage.jpa;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;

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
        this.post = Post.builder()
            .title("title")
            .content("content")
            .visible(true)
            .build();
    }

    /********************************************************************************
                    Post Entity에 대한 CRUD 수행을 테스트한다.

    Create
        {@link Test_CreateWithRead_Post#Success_PostWithNoCategory_Create}
            - 카테고리가 없는 포스트 추가
        {@link Test_CreateWithRead_Post#Success_PostWithCategory_Create}
            - 카테고리가 있는 포스트 추가

    Update
        {@link Test_Update_Post#Success_PostDetail_Update}
            - 포스트의 내용 변경
    Delete
        {@link Test_Delete_Post#Success_OnePost_Delete}
            - 아디디에 맞는 포스트 삭제
*********************************************************************************/

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
            Post savedPost = tem.find(Post.class, post.getPostIdx());

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
            Category category = Category.builder()
                .name("category")
                .build();
            tem.persist(category);
            post.setCategory(category);
            tem.persist(post);
            tem.clear();

            // when
            Post savedPost = tem.find(Post.class, post.getPostIdx());

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
            Post targetPost = tem.find(Post.class, post.getPostIdx());
            targetPost.setTitle(titleChanged);
            targetPost.setVisible(false);

            tem.merge(targetPost);
            tem.flush();
            tem.clear();

            Post updatedPost = tem.find(Post.class, post.getPostIdx());

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
            tem.remove(tem.find(Post.class, post.getPostIdx()));
            tem.flush();
            tem.clear();
            Throwable thrown = catchThrowable(() -> tem.find(Post.class, post.getPostIdx()));

            // then
            assertThat(thrown)
                .isNull();
        }
    }
}
