package io.vekzzdev.personal_blog_lite.service;

import io.vekzzdev.personal_blog_lite.exception.*;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PostService")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 30, 12, 0, 0);

    @Mock
    private PostRepository postRepository;

    @Mock
    private MarkdownService markdownService;

    @InjectMocks
    private PostService postService;

    // --- Helpers ---

    private Post post(int id, String title, String content) {
        return new Post(id, title, content, NOW, NOW);
    }

    // --- getAllPosts ---

    @Nested
    @DisplayName("getAllPosts")
    class GetAllPostsTests {

        @Test
        @DisplayName("should return all posts from repository")
        void shouldReturnAllPosts_whenRepositoryReturnsPosts() {
            var posts = List.of(
                    post(1, "First", "Content 1"),
                    post(2, "Second", "Content 2")
            );
            when(postRepository.findAll()).thenReturn(posts);

            List<Post> result = postService.getAllPosts();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("First");
            assertThat(result.get(1).getTitle()).isEqualTo("Second");
            verify(postRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no posts exist")
        void shouldReturnEmptyList_whenNoPosts() {
            when(postRepository.findAll()).thenReturn(List.of());

            List<Post> result = postService.getAllPosts();

            assertThat(result).isEmpty();
        }
    }

    // --- getPostById ---

    @Nested
    @DisplayName("getPostById")
    class GetPostByIdTests {

        @Test
        @DisplayName("should return post when id exists")
        void shouldReturnPost_whenIdExists() {
            var post = post(1, "Test", "Content");
            when(postRepository.findById(1)).thenReturn(Optional.of(post));

            Post result = postService.getPostById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("Test");
        }

        @Test
        @DisplayName("should throw PostNotFoundException when id not found")
        void shouldThrowPostNotFoundException_whenIdNotFound() {
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostById(999))
                    .isInstanceOf(PostNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // --- createPost ---

    @Nested
    @DisplayName("createPost")
    class CreatePostTests {

        @Test
        @DisplayName("should create and return post with generated id")
        void shouldCreatePost_whenValidInput() {
            var savedPost = post(1, "New", "Content");
            when(postRepository.insert(any(Post.class))).thenReturn(savedPost);

            postService.createPost("New", "Content");

            verify(postRepository).insert(argThat(post ->
                    post.getTitle().equals("New") &&
                    post.getContent().equals("Content")
            ));
        }

        @Test
        @DisplayName("should throw PostCreationException when repository throws")
        void shouldThrowPostCreationException_whenRepositoryThrows() {
            when(postRepository.insert(any(Post.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> postService.createPost("New", "Content"))
                    .isInstanceOf(PostCreationException.class);
        }

        @Test
        @DisplayName("should throw PostCreationException when returned post has null id")
        void shouldThrowPostCreationException_whenReturnedIdIsNull() {
            var postWithNullId = new Post(null, "New", "Content", NOW, NOW);
            when(postRepository.insert(any(Post.class))).thenReturn(postWithNullId);

            assertThatThrownBy(() -> postService.createPost("New", "Content"))
                    .isInstanceOf(PostCreationException.class);
        }
    }

    // --- updatePost ---

    @Nested
    @DisplayName("updatePost")
    class UpdatePostTests {

        @Test
        @DisplayName("should update post when exists")
        void shouldUpdatePost_whenPostExists() {
            var existingPost = post(1, "Old", "Old Content");
            when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));
            when(postRepository.update(any(Post.class))).thenReturn(1);

            postService.updatePost(1, "Updated", "New Content");

            verify(postRepository).update(argThat(post ->
                    post.getId() == 1 &&
                    post.getTitle().equals("Updated") &&
                    post.getContent().equals("New Content")
            ));
        }

        @Test
        @DisplayName("should throw PostNotFoundException when post not found")
        void shouldThrowPostNotFoundException_whenPostNotFound() {
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.updatePost(999, "Title", "Content"))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("should throw PostUpdatingException when repository throws")
        void shouldThrowPostUpdatingException_whenRepositoryThrows() {
            var existingPost = post(1, "Old", "Old Content");
            when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));
            when(postRepository.update(any(Post.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> postService.updatePost(1, "New", "Content"))
                    .isInstanceOf(PostUpdatingException.class);
        }
    }

    // --- deletePost ---

    @Nested
    @DisplayName("deletePost")
    class DeletePostTests {

        @Test
        @DisplayName("should delete post when exists")
        void shouldDeletePost_whenPostExists() {
            when(postRepository.delete(1)).thenReturn(1);

            postService.deletePost(1);

            verify(postRepository).delete(1);
        }

        @Test
        @DisplayName("should throw PostDeletingException when post not found")
        void shouldThrowPostDeletingException_whenPostNotFound() {
            when(postRepository.delete(999)).thenReturn(0);

            assertThatThrownBy(() -> postService.deletePost(999))
                    .isInstanceOf(PostDeletingException.class);
        }

        @Test
        @DisplayName("should throw PostDeletingException when repository throws")
        void shouldThrowPostDeletingException_whenRepositoryThrows() {
            when(postRepository.delete(1))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> postService.deletePost(1))
                    .isInstanceOf(PostDeletingException.class);
        }
    }

    // --- renderContentPostToHtml ---

    @Nested
    @DisplayName("renderContentPostToHtml")
    class RenderContentPostToHtmlTests {

        @Test
        @DisplayName("should return HTML when markdown is valid")
        void shouldReturnHtml_whenMarkdownIsValid() {
            when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");

            String result = postService.renderContentPostToHtml("# Hello");

            assertThat(result).isEqualTo("<h1>Hello</h1>");
        }

        @Test
        @DisplayName("should return empty string when markdown is blank")
        void shouldReturnEmptyString_whenMarkdownIsBlank() {
            when(markdownService.toHtml("")).thenReturn("");
            when(markdownService.toHtml("   ")).thenReturn("");

            assertThat(postService.renderContentPostToHtml("")).isEmpty();
            assertThat(postService.renderContentPostToHtml("   ")).isEmpty();
        }

        @Test
        @DisplayName("should throw PostRenderingException when service throws")
        void shouldThrowPostRenderingException_whenServiceThrows() {
            when(markdownService.toHtml(anyString()))
                    .thenThrow(new RuntimeException("Parse error"));

            assertThatThrownBy(() -> postService.renderContentPostToHtml("# Hi"))
                    .isInstanceOf(PostRenderingException.class);
        }
    }
}
