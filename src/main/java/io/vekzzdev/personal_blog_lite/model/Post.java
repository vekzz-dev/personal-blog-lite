package io.vekzzdev.personal_blog_lite.model;

import java.time.LocalDateTime;

public record Post(
        int id,
        String title,
        String content,
        LocalDateTime created_at,
        LocalDateTime updated_at
) {
}
