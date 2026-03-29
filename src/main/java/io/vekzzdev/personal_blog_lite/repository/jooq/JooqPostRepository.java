package io.vekzzdev.personal_blog_lite.repository.jooq;

import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.repository.PostRepository;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Optional;

import static io.vekzzdev.generated.jooq.Tables.POSTS;

public class JooqPostRepository implements PostRepository {

    private final DSLContext dsl;

    public JooqPostRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Post> findAll() {
        return dsl.selectFrom(POSTS)
                .fetchInto(Post.class);
    }

    @Override
    public Optional<Post> findById(int id) {
        return Optional.ofNullable(
                dsl.selectFrom(POSTS)
                        .where(POSTS.ID.eq(id))
                        .fetchOneInto(Post.class)
        );
    }

    @Override
    public Post insert(Post post) {
        return dsl.insertInto(POSTS)
                .set(POSTS.TITLE, post.title())
                .set(POSTS.CONTENT, post.content())
                .set(POSTS.CREATED_AT, post.created_at())
                .set(POSTS.UPDATED_AT, post.updated_at())
                .returning()
                .fetchOneInto(Post.class);
    }

    @Override
    public int update(Post post) {
        return dsl.update(POSTS)
                .set(POSTS.TITLE, post.title())
                .set(POSTS.CONTENT, post.content())
                .set(POSTS.CREATED_AT, post.created_at())
                .set(POSTS.UPDATED_AT, post.updated_at())
                .where(POSTS.ID.eq(post.id()))
                .execute();
    }

    @Override
    public int delete(int id) {
        return dsl.deleteFrom(POSTS)
                .where(POSTS.ID.eq(id))
                .execute();
    }
}
