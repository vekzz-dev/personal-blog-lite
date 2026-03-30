package io.vekzzdev.personal_blog_lite.repository.jooq;

import io.vekzzdev.personal_blog_lite.model.Post;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.vekzzdev.generated.jooq.Tables.POSTS;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JooqPostRepository")
class JooqPostRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 29, 12, 0, 0);

    private final LinkedList<MockResult[]> responses = new LinkedList<>();

    private final MockDataProvider provider = ctx -> responses.removeFirst();

    private final DSLContext buildDsl = DSL.using(SQLDialect.MARIADB);

    private JooqPostRepository repository;

    @BeforeEach
    void setUp() {
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.MARIADB);
        repository = new JooqPostRepository(dsl);
        responses.clear();
    }

    // --- Helpers ---

    private MockResult mockSelectResult(Post... posts) {
        var result = buildDsl.newResult(POSTS);
        for (Post post : posts) {
            var record = buildDsl.newRecord(POSTS);
            record.set(POSTS.ID, post.getId());
            record.set(POSTS.TITLE, post.getTitle());
            record.set(POSTS.CONTENT, post.getContent());
            record.set(POSTS.CREATED_AT, post.getCreatedAt());
            record.set(POSTS.UPDATED_AT, post.getUpdatedAt());
            result.add(record);
        }
        return new MockResult(result.size(), result);
    }

    private MockResult mockDmlResult(int rowsAffected) {
        return new MockResult(rowsAffected, null);
    }

    private Post post(int id, String title, String content) {
        return new Post(id, title, content, NOW, NOW);
    }

    // --- findAll ---

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("should return all posts when posts exist")
        void shouldReturnAllPosts_whenPostsExist() {
            responses.add(new MockResult[]{mockSelectResult(
                    post(1, "First", "Content 1"),
                    post(2, "Second", "Content 2")
            )});

            List<Post> result = repository.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.getFirst()).isEqualTo(post(1, "First", "Content 1"));
            assertThat(result.get(1)).isEqualTo(post(2, "Second", "Content 2"));
        }

        @Test
        @DisplayName("should return empty list when no posts exist")
        void shouldReturnEmptyList_whenNoPosts() {
            responses.add(new MockResult[]{mockSelectResult()});

            List<Post> result = repository.findAll();

            assertThat(result).isEmpty();
        }
    }

    // --- findById ---

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return post when id exists")
        void shouldReturnPost_whenIdExists() {
            responses.add(new MockResult[]{mockSelectResult(
                    post(1, "Test", "Content")
            )});

            Optional<Post> result = repository.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(post(1, "Test", "Content"));
        }

        @Test
        @DisplayName("should return empty optional when id not found")
        void shouldReturnEmptyOptional_whenIdNotFound() {
            responses.add(new MockResult[]{mockSelectResult()});

            Optional<Post> result = repository.findById(999);

            assertThat(result).isEmpty();
        }
    }

    // --- insert ---

    @Nested
    @DisplayName("insert")
    class InsertTests {

        @Test
        @DisplayName("should insert and return post with generated id")
        void shouldInsertAndReturnPost_withGeneratedId() {
            Post toInsert = post(0, "New Post", "New Content");
            responses.add(new MockResult[]{mockSelectResult(
                    post(1, "New Post", "New Content")
            )});

            Post result = repository.insert(toInsert);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("New Post");
            assertThat(result.getContent()).isEqualTo("New Content");
        }
    }

    // --- update ---

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("should return 1 when post exists")
        void shouldReturnOne_whenPostExists() {
            responses.add(new MockResult[]{mockDmlResult(1)});

            int result = repository.update(post(1, "Updated", "Updated content"));

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 0 when post not found")
        void shouldReturnZero_whenPostNotFound() {
            responses.add(new MockResult[]{mockDmlResult(0)});

            int result = repository.update(post(999, "Updated", "Updated content"));

            assertThat(result).isEqualTo(0);
        }
    }

    // --- delete ---

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("should return 1 when id exists")
        void shouldReturnOne_whenIdExists() {
            responses.add(new MockResult[]{mockDmlResult(1)});

            int result = repository.delete(1);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 0 when id not found")
        void shouldReturnZero_whenIdNotFound() {
            responses.add(new MockResult[]{mockDmlResult(0)});

            int result = repository.delete(999);

            assertThat(result).isEqualTo(0);
        }
    }
}
