package io.vekzzdev.personal_blog_lite.exception;

public class PostCreationException extends DomainException {

    public PostCreationException() {
        super("Post creation failed, try again");
    }

    public PostCreationException(Throwable cause) {
        super("Post creation failed, try again", cause);
    }
}
