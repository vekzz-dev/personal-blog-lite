package io.vekzzdev.personal_blog_lite.exception;

public class PostDeletingException extends DomainException {

    public PostDeletingException() {
        super("Post deletion failed, try again");
    }

    public PostDeletingException(Throwable cause) {
        super("Post deletion failed, try again", cause);
    }
}
