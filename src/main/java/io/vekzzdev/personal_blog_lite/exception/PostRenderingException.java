package io.vekzzdev.personal_blog_lite.exception;

public class PostRenderingException extends DomainException {

    public PostRenderingException() {
        super("Error while rendering post");
    }

    public PostRenderingException(Throwable cause) {
        super("Error while rendering post", cause);
    }
}
