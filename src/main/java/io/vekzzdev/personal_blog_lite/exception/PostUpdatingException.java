package io.vekzzdev.personal_blog_lite.exception;

public class PostUpdatingException extends DomainException {

    public PostUpdatingException() {
        super("Error while updating post, try again");
    }

    public PostUpdatingException(Throwable cause) {
        super("Error while updating post, try again", cause);
    }
}
