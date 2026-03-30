package io.vekzzdev.personal_blog_lite.service;

import io.vekzzdev.personal_blog_lite.exception.*;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final MarkdownService markdownService;


    public PostService(PostRepository postRepository, MarkdownService markdownService) {
        this.postRepository = postRepository;
        this.markdownService = markdownService;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(int id) {
        var post = postRepository.findById(id);
        return post.orElseThrow(() -> new PostNotFoundException(id));
    }

    public void createPost(String title, String content) {
        var now = LocalDateTime.now();
        var post = new Post(title, content, now, now);

        try {
            post = postRepository.insert(post);

        } catch (RuntimeException e) {
            logger.error("Error while creating post", e);
            throw new PostCreationException(e);
        }

        if (post == null || post.getId() == null || post.getId() < 1) {
            throw new PostCreationException();
        }
    }

    public void updatePost(int id, String title, String content) {
        var post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        var now = LocalDateTime.now();
        post.setTitle(title);
        post.setContent(content);
        post.setUpdatedAt(now);

        int rows = 0;
        try {
            rows = postRepository.update(post);

        } catch (RuntimeException e) {
            logger.error("Error while updating post", e);
            throw new PostUpdatingException(e);
        }

        if (rows != 1) throw new PostUpdatingException();
    }

    public void deletePost(int id) {
        int rows = 0;

        try {
            rows = postRepository.delete(id);

        } catch (RuntimeException e) {
            logger.error("Error while deleting post", e);
            throw new PostDeletingException(e);
        }

        if (rows != 1) throw new PostDeletingException();
    }

    public String renderContentPostToHtml(String contentMarkdown) {
        try {
            return markdownService.toHtml(contentMarkdown);

        } catch (RuntimeException e) {
            logger.error("Error while rendering post", e);
            throw new PostRenderingException(e);
        }
    }
}
