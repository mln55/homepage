package com.personalproject.homepage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class PostRepositoryTest {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    private final int testPage = 0;
    private final int testSize = 8;
    private final Sort testSort = Sort.by(Direction.DESC, "createAt");
    private final Pageable testPageable = PageRequest.of(testPage, testSize, testSort);
    private final Comparator<Post> createAtDescComp = (p1, p2) -> p1.getCreateAt().isAfter(p2.getCreateAt()) ? -1 : 1;

    private Category savedParentCategory;
    private Category savedChildCategory;
    private Post savedPost;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    @BeforeEach
    void resetPersist() {
        // parent
        //  └ child1

        savedParentCategory = MockEntity.mock(Category.class);
        savedParentCategory.updateInfo("parent", null);
        categoryRepository.save(savedParentCategory);

        savedChildCategory = MockEntity.mock(Category.class);
        savedChildCategory.updateInfo("child", savedParentCategory);
        categoryRepository.save(savedChildCategory);

        savedPost = MockEntity.mock(Post.class);
        savedPost.updateInfo(savedParentCategory, "title", "content", true);
        postRepository.save(savedPost);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Post {
        @Test
        @DisplayName("성공: 카테고리가 없는 포스트를 추가한다.")
        void Success_PostWithNoCategory_Create() {
            // given
            Post post = MockEntity.mock(Post.class);
            post.updateInfo(null, "title", "content", true);

            // when
            Post savedPost = postRepository.save(post);

            // then
            assertThat(savedPost)
                .extracting("idx")
                .isNotNull();
            assertThat(savedPost)
                .extracting("category")
                .isNull();
        }

        @Test
        @DisplayName("성공: 카테고리가 있는 포스트를 추가한다.")
        void Success_PostWithCategory_Create() {
            // given - savedChildCategory
            Post post = MockEntity.mock(Post.class);
            post.updateInfo(savedChildCategory, "title", "content", true);

            // when
            Post newPost = postRepository.save(post);

            // then
            assertThat(newPost)
                .extracting("idx")
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
            Optional<Post> post = postRepository.findById(savedPost.getIdx());

            // then
            assertThat(post)
                .isPresent();
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 List<Post>를 반환한다.")
        void Success_PostsPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(null, "title" + i, "content" + i, true);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAll(testPageable);

            // then
            assertThat(postList)
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
                .size()
                .isEqualTo(testSize);
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisiblePerPage_ReturnPostList() {
            // given - total 11 posts but 6 are visible including savedPost
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(null, "title" + i, "content" + i, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleTrue(testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == false인 List<Post>를 반환한다.")
        void Success_PostsInvisiblePerPage_ReturnPostList() {
            // given - total 11 posts but 5 are invisible including savedPost
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(null, "title" + i, "content" + i, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleFalse(testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> !p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
                .size()
                .isEqualTo(5);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 List<Post>를 반환한다.")
        void Success_PostsByCategoryPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost in same category
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedParentCategory, "title" + i, "content" + i, true);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByCategory(savedParentCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().getName().equals(savedParentCategory.getName()))
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
                .size()
                .isEqualTo(testSize);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisibleByCategoryPerPage_ReturnPostList() {
            // given - total 10 posts but 5 are visible in savedChildCategory
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleTrueAndCategory(savedChildCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
                .size()
                .isEqualTo(5);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible == false인 List<Post>를 반환한다.")
        void Success_PostsIsInvisibleByCategoryPerPage_ReturnPostList() {
            // given - total 10 posts but 5 are invisible in savedChildCategory
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleFalseAndCategory(savedChildCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> !p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, testSize)
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
            savedPost.updateInfo(savedChildCategory, title, content, visible);

            Post updatedPost = postRepository.findById(savedPost.getIdx()).orElseThrow();

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
            Optional<Post> deletedPost = postRepository.findById(savedPost.getIdx());

            // then
            assertThat(deletedPost)
                .isNotPresent();
        }
    }
}
