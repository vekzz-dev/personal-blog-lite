package io.vekzzdev.personal_blog_lite.repository;

import io.vekzzdev.personal_blog_lite.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    List<Post> findAll();

    Optional<Post> findById(int id);

    Post insert(Post post);

    int update(Post post);

    int delete(int id);
}
