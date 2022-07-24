package com.personalproject.homepage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;

import com.personalproject.homepage.entity.Category;
import com.personalproject.homepage.entity.Post;
import com.personalproject.homepage.entity.groupby.PostsCountByCategory;
import com.personalproject.homepage.helper.MockEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class PostRepositoryTest {

    private final static int TEST_PAGE = 0;
    private final static int TEST_SIZE = 8;
    private final static Sort testSort = Sort.by(Direction.DESC, "createAt");
    private final static Pageable testPageable = PageRequest.of(TEST_PAGE, TEST_SIZE, testSort);
    private final static Comparator<Post> createAtDescComp = (p1, p2) -> p1.getCreateAt().isAfter(p2.getCreateAt()) ? -1 : 1;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    private Category savedParentCategory;
    private Category savedChildCategory;
    private Category savedChildCategory2;
    private Post savedPost;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    @BeforeEach
    void resetPersist() {
        // parent
        //  └ child

        savedParentCategory = MockEntity.mock(Category.class);
        savedParentCategory.updateInfo("parent", null);
        categoryRepository.save(savedParentCategory);

        savedChildCategory = MockEntity.mock(Category.class);
        savedChildCategory.updateInfo("child", savedParentCategory);
        categoryRepository.save(savedChildCategory);

        savedChildCategory2 = MockEntity.mock(Category.class);
        savedChildCategory2.updateInfo("child2", savedParentCategory);
        categoryRepository.save(savedChildCategory2);

        savedPost = MockEntity.mock(Post.class);
        savedPost.updateInfo(savedChildCategory, "title", "content", "desc", true);
        postRepository.save(savedPost);
    }

    @Nested
    @DisplayName("Create")
    class Test_Create_Post {
        @Test
        @DisplayName("성공: 포스트를 추가한다.")
        void Success_PostWithCategory_Create() {
            // given - savedChildCategory
            Post post = MockEntity.mock(Post.class);
            post.updateInfo(savedChildCategory, "title", "content", "desc", true);

            // when
            Post newPost = postRepository.save(post);

            // then
            assertThat(newPost)
                .extracting("idx")
                .isNotNull();
            assertThat(newPost)
                .extracting("category")
                .isNotNull()
                .extracting("name", "parentCategory.name")
                .containsExactly(savedChildCategory.getName(), savedParentCategory.getName());
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
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, "desc" + i, true);
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
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisiblePerPage_ReturnPostList() {
            // given - total 11 posts but 6 are visible including savedPost
            Boolean visible = true;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(null, "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisible(visible, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 페이지에 맞는 visible == false인 List<Post>를 반환한다.")
        void Success_PostsInvisiblePerPage_ReturnPostList() {
            // given - total 11 posts but 5 are invisible including savedPost
            Boolean visible = false;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(null, "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisible(visible, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> !p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(5);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 List<Post>를 반환한다.")
        void Success_PostsByCategoryPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost in same category
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, "desc" + 1, true);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByCategory(savedChildCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().equals(savedChildCategory))
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible == true인 List<Post>를 반환한다.")
        void Success_PostsIsVisibleByCategoryPerPage_ReturnPostList() {
            // given - total 10 posts but 6 are visible in savedChildCategory
            Boolean visible = true;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleAndCategory(visible, savedChildCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().equals(savedChildCategory) && p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 카테고리, 페이지에 맞는 visible == false인 List<Post>를 반환한다.")
        void Success_PostsIsInvisibleByCategoryPerPage_ReturnPostList() {
            // given - total 10 posts but 5 are invisible in savedChildCategory
            Boolean visible = false;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleAndCategory(visible, savedChildCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().equals(savedChildCategory) && !p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(5);
        }


        @Test
        @DisplayName("성공: 상위 카테고리의 하위 카테고리에 속한 포스트 리스트를 반환한다.")
        void Success_PostsByParentCategoryPerPage_ReturnPostList() {
            // given - total 11 posts including savedPost in categories of parent category
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(i % 2 == 0 ? savedChildCategory : savedChildCategory2,
                    "title" + i, "content" + i, "desc" + 1, true);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByCategory_ParentCategory(savedParentCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().getParentCategory().equals(savedParentCategory))
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(TEST_SIZE);
        }

        @Test
        @DisplayName("성공: 상위 카테고리의 하위 카테고리에 속한 visible 포스트 리스트를 반환한다.")
        void Success_PostsIsVisibleByParentCategoryPerPage_ReturnPostList() {
            // given - total 6 visible posts including savedPost in categories of parent category
            Boolean visible = true;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(i % 2 == 0 ? savedChildCategory : savedChildCategory2,
                    "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleAndCategory_ParentCategory(visible, savedParentCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().getParentCategory().equals(savedParentCategory) && p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(6);
        }

        @Test
        @DisplayName("성공: 상위 카테고리의 하위 카테고리에 속한 invisible 포스트 리스트를 반환한다.")
        void Success_PostsIsInVisibleByParentCategoryPerPage_ReturnPostList() {
            // given - total 5 invisible posts in categories of parent category
            Boolean visible = false;
            for (int i = 0; i < 10; ++i) {
                Post p = MockEntity.mock(Post.class);
                p.updateInfo(savedChildCategory, "title" + i, "content" + i, "desc" + 1, i % 2 == 0);
                postRepository.save(p);
                try {
                    Thread.sleep(30);
                } catch (Exception e) {/** empty */}
            }

            // when
            List<Post> postList = postRepository.findAllByVisibleAndCategory_ParentCategory(visible, savedParentCategory, testPageable);

            // then
            assertThat(postList)
                .allMatch(p -> p.getCategory().getParentCategory().equals(savedParentCategory) && !p.getVisible())
                .isSortedAccordingTo(createAtDescComp)
                .hasSizeBetween(0, TEST_SIZE)
                .size()
                .isEqualTo(5);
        }

        @Test
        @DisplayName("성공: 카테고리별 포스트 개수를 반환한다.")
        void Success_CountPostsPerCategory_ReturnObjectList() {
            // given - savedChildCategory, savedChildCategory2
            // 6 posts in first, 5 in second
            Boolean visible = true;
            IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    Post p = MockEntity.mock(Post.class);
                    p.updateInfo(i % 2 == 1 ? savedChildCategory : savedChildCategory2,
                        "title", "content", "desc", visible);
                    postRepository.save(p);
                });

            // when
            List<PostsCountByCategory> postCountList = postRepository.countAllGroupByCategory();

            // then
            assertThat(postCountList).size().isEqualTo(3);
            assertThat(postCountList)
                .extracting("category")
                .doesNotHaveDuplicates();
            assertThat(postCountList).allMatch(pc ->
                (
                    pc.getCategory().equals(savedParentCategory)
                    && pc.getVisibleCount() == 0 && pc.getInvisibleCount() == 0
                ) ||
                (
                    pc.getCategory().equals(savedChildCategory)
                    && pc.getVisibleCount() == 6 && pc.getInvisibleCount() == 0
                ) ||
                (
                    pc.getCategory().equals(savedChildCategory2)
                    && pc.getVisibleCount() == 5 && pc.getInvisibleCount() == 0
                )
            );
        }
    }

    @Nested
    @DisplayName("Update")
    class Test_Update_Post {
        @Test
        @DisplayName("성공: 포스트 내용을 변경한다.")
        void Success_PostDetail_Update() {
            // given - savedPost, savedChildCategory2
            String title = "changed";
            String content = "changed";
            String desc = "changed";
            boolean visible = false;

            // when
            savedPost.updateInfo(savedChildCategory2, title, content, desc, visible);

            Post updatedPost = postRepository.findById(savedPost.getIdx()).orElseThrow();

            /********************************************************************************
                @DataJpaTest가 붙은 Test class는 transactional하며
                persistent context에 정보가 저장되므로 id로 post조회 시 db로 select하지 않는다.
                해당 메소드가 끝날 때 update쿼리를 실행하므로 해당 테스트에서 post.updateAt == null
            ********************************************************************************/
            // then
            assertThat(updatedPost)
                .extracting("title", "content", "visible", "category.name", "category.parentCategory.name")
                .containsExactly(title, content, visible, savedChildCategory2.getName(), savedParentCategory.getName());
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
