package io.vekzzdev.personal_blog_lite.exception;

public class PostNotFoundException extends DomainException {

    public PostNotFoundException(int id) {
        super("Post not found: " + id);
    }
}
