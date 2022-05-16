package com.personalproject.homepage.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.paging.Pagination;

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
public class PostRepositoryTest {

    private Category savedParentCategory;
    private Category savedChildCategory;
    private Post savedPost;
    private Comparator<Post> createAtDescComp = (p1, p2) -> p1.getCreateAt().isAfter(p2.getCreateAt()) ? -1 : 1;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    /********************************************************************************
                    PostyRepository의 상황에 따른 동작을 테스트 한다.

    {@link #resetPersist}
        - @BeforeEach 카테고리, 포스트를 test db에 insert한다.

    Create
        {@link Test_Create_Post#Success_PostWithNoCategory_Create}
            - 카테고리가 없는 포스트를 추가한다.
        {@link Test_Create_Post#Success_PostWithCategory_Create}
            - 카테고리가 있는 포스트를 추가한다.

    Read
        {@link Test_Read_Post#Success_OnePost_ReturnPostOptional}
            - 포스트 하나를 Optional<Post>로 반환한다.
        {@link Test_Read_Post#Success_PostsPerPage_ReturnPostList}
            - 페이지에 맞는 List<Post>를 반환한다.
        {@link Test_Read_Post#Success_PostsIsVisiblePerPage_ReturnPostList}
            - 페이지에 맞는 visible == ture인 List<Post>를 반환한다.
        {@link Test_Read_Post#Success_PostsByCategoryPerPage_ReturnPostList}
            - 카테고리, 페이지에 맞는 List<Post>를 반환한다.
        {@link Test_Read_Post#Success_PostsIsVisibleByCategoryPerPage_ReturnPostList}
            - 카테고리, 페이지에 visible == true인 List<Post>를 반환한다.

    Update
        {@link Test_Update_Post#Success_PostDetail_Update}
            - 포스트 내용을 변경한다.

    Delete
        {@link Test_Delete_Post#Success_OnePost_Delete}
            - 포스트를 삭제한다.
    ********************************************************************************/

    @BeforeEach
    void resetPersist() {
        // parent
        //  └ child1
        savedParentCategory = Category.builder()
            .name("parent")
            .build();
        categoryRepository.save(savedParentCategory);

        savedChildCategory = Category.builder()
            .name("child")
            .parentCategory(savedParentCategory)
            .build();
        categoryRepository.save(savedChildCategory);

        savedPost = Post.builder()
            .title("title")
            .content("content")
            .visible(true)
            .category(savedParentCategory)
            .build();
        postRepository.save(savedPost);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Post {
        @Test
        @DisplayName("성공: 카테고리가 없는 포스트를 추가한다.")
        void Success_PostWithNoCategory_Create() {
            // given
            Post post = Post.builder()
                .title("title")
                .content("content")
                .visible(true)
                .build();

            // when
            Post savedPost = postRepository.save(post);

            // then
            assertThat(savedPost)
                .extracting("postIdx")
                .isNotNull();
            assertThat(savedPost)
                .extracting("category")
                .isNull();
        }

        @Test
        @DisplayName("성공: 카테고리가 있는 포스트를 추가한다.")
        void Success_PostWithCategory_Create() {
            // given - savedChildCategory
            Post post = Post.builder()
                .title("title")
                .content("content")
                .visible(true)
                .category(savedChildCategory)
                .build();

            // when
            Post newPost = postRepository.save(post);

            // then
            assertThat(newPost)
                .extracting("postIdx")
                .isNotNull();
            assertThat(newPost)
                .extracting("category")
                .isNotNull()
                .extracting("name")
                .isEqualTo(savedChildCategory.getName());
        }
    }

    @Nested
    @DisplayName("Read")
    class Test_Read_Post {
        @Test
        @DisplayName("성공: 포스트 하나를 Optional<Post>로 반환한다.")
        void Success_OnePost_ReturnPostOptional() {
            // given - savedPost

            // when
            Optional<Post> post = postRepository.findById(savedPost.getPostIdx());

            // then
            assertThat(post)
                .isPresent();
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 List<Post>를 반환한다.")
        void Success_PostsPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost
            for (int i = 0; i < 10; ++i) {
                postRepository.save(Post.builder()
                    .title("title" + i)
                    .content("content" + i)
                    .visible(true)
                    .build());
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAll(Pagination.DEFAULT_PAGEREQUEST(1));

            // then
            assertThat(postList)
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, Pagination.DEFAULT_PAGE_SIZE)
                .size()
                .isEqualTo(Pagination.DEFAULT_PAGE_SIZE);
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisiblePerPage_ReturnPostList() {
            // given - total 11 posts but 6 are visible including savedPost
            for (int i = 0; i < 10; ++i) {
                postRepository.save(Post.builder()
                    .title("title" + i)
                    .content("content" + i)
                    .visible(i % 2 == 0)
                    .build());
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleTrue(Pagination.DEFAULT_PAGEREQUEST(1));

            // then
            assertThat(postList)
                .allMatch(p -> p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, Pagination.DEFAULT_PAGE_SIZE)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 List<Post>를 반환한다.")
        void Success_PostsByCategoryPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost in same category
            for (int i = 0; i < 10; ++i) {
                postRepository.save(Post.builder()
                    .title("title" + i)
                    .content("content" + i)
                    .category(savedParentCategory)
                    .visible(true)
                    .build());
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByCategory(savedParentCategory, Pagination.DEFAULT_PAGEREQUEST(1));

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().getName().equals(savedParentCategory.getName()))
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, Pagination.DEFAULT_PAGE_SIZE)
                .size()
                .isEqualTo(Pagination.DEFAULT_PAGE_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisibleByCategoryPerPage_ReturnPostList() {
            // given - total 10 posts but 5 are visible in savedChildCategory
            for (int i = 0; i < 10; ++i) {
                postRepository.save(Post.builder()
                    .title("title" + i)
                    .content("content" + i)
                    .category(savedChildCategory)
                    .visible(i % 2 == 0)
                    .build());
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleTrueAndCategory(savedChildCategory, Pagination.DEFAULT_PAGEREQUEST(1));

            // then
            assertThat(postList)
                .allMatch(p -> p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, Pagination.DEFAULT_PAGE_SIZE)
                .size()
                .isEqualTo(5);
        }

    }
    @Nested
    @DisplayName("Update")
    class Test_Update_Post {
        @Test
        @DisplayName("성공: 포스트 내용을 변경한다.")
        void Success_PostDetail_Update() {
            // given - savedPost, savedChildCategory
            String title = "changed";
            String content = "changed";
            boolean visible = false;

            // when
            savedPost.setTitle(title);
            savedPost.setContent(content);
            savedPost.setVisible(visible);
            savedPost.setCategory(savedChildCategory);
            postRepository.save(savedPost);

            Post updatedPost = postRepository.findById(savedPost.getPostIdx()).orElseThrow();

            /********************************************************************************
                @DataJpaTest가 붙은 Test class는 transactional하며
                persistent context에 정보가 저장되므로 id로 post조회 시 db로 select하지 않는다.
                해당 메소드가 끝날 때 update쿼리를 실행하므로 해당 테스트에서 post.updateAt == null
            ********************************************************************************/
            // then
            assertThat(updatedPost)
                .extracting("title", "content", "visible", "category.name")
                .containsExactly(title, content, visible, savedChildCategory.getName());
            assertThat(updatedPost)
                .extracting("updateAt")
                .isNull();
        }
    }
    @Nested
    @DisplayName("Delete")
    class Test_Delete_Post {
        @Test
        @DisplayName("성공: 포스트를 삭제한다.")
        void Success_OnePost_Delete() {
            // given - savedPost

            // when
            postRepository.delete(savedPost);
            Optional<Post> deletedPost = postRepository.findById(savedPost.getPostIdx());

            // then
            assertThat(deletedPost)
                .isNotPresent();
        }
    }
}
