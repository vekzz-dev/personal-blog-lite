package io.vekzzdev.personal_blog_lite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MarkdownService")
class MarkdownServiceTest {

    private MarkdownService markdownService;

    @BeforeEach
    void setUp() {
        markdownService = new MarkdownService();
    }

    @Nested
    @DisplayName("toHtml")
    class ToHtmlTests {

        @Test
        @DisplayName("should convert heading markdown to HTML")
        void shouldConvertHeading_toHtml() {
            String result = markdownService.toHtml("# Hello World");

            assertThat(result).contains("Hello World");
        }

        @Test
        @DisplayName("should convert bold markdown to HTML")
        void shouldConvertBold_toHtml() {
            String result = markdownService.toHtml("**bold text**");

            assertThat(result).contains("<strong>");
            assertThat(result).contains("bold text");
        }

        @Test
        @DisplayName("should convert italic markdown to HTML")
        void shouldConvertItalic_toHtml() {
            String result = markdownService.toHtml("*italic text*");

            assertThat(result).contains("<em>");
            assertThat(result).contains("italic text");
        }

        @Test
        @DisplayName("should convert link markdown to HTML")
        void shouldConvertLink_toHtml() {
            String result = markdownService.toHtml("[Google](https://google.com)");

            assertThat(result).contains("href=\"https://google.com\"");
            assertThat(result).contains("Google");
        }

        @Test
        @DisplayName("should convert code block to HTML")
        void shouldConvertCodeBlock_toHtml() {
            String result = markdownService.toHtml("```java\nSystem.out.println();\n```");

            assertThat(result).contains("<pre>");
            assertThat(result).contains("<code");
        }

        @Test
        @DisplayName("should convert inline code to HTML")
        void shouldConvertInlineCode_toHtml() {
            String result = markdownService.toHtml("Use `System.out.println()`");

            assertThat(result).contains("<code>");
        }

        @Test
        @DisplayName("should convert list markdown to HTML")
        void shouldConvertList_toHtml() {
            String result = markdownService.toHtml("- Item 1\n- Item 2");

            assertThat(result).contains("<ul>");
            assertThat(result).contains("<li>");
            assertThat(result).contains("Item 1");
        }

        @Test
        @DisplayName("should convert blockquote to HTML")
        void shouldConvertBlockquote_toHtml() {
            String result = markdownService.toHtml("> This is a quote");

            assertThat(result).contains("<blockquote>");
            assertThat(result).contains("This is a quote");
        }

        @Test
        @DisplayName("should return empty string when input is null")
        void shouldReturnEmptyString_whenInputIsNull() {
            String result = markdownService.toHtml(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty string when input is blank")
        void shouldReturnEmptyString_whenInputIsBlank() {
            assertThat(markdownService.toHtml("")).isEmpty();
            assertThat(markdownService.toHtml("   ")).isEmpty();
            assertThat(markdownService.toHtml("\t\n")).isEmpty();
        }

        @Nested
        @DisplayName("sanitization")
        class SanitizationTests {

            @Test
            @DisplayName("should remove script tags from HTML")
            void shouldRemoveScriptTags() {
                String result = markdownService.toHtml("<script>alert('xss')</script>");

                assertThat(result).doesNotContain("<script>");
                assertThat(result).doesNotContain("alert");
            }

            @Test
            @DisplayName("should remove onclick handlers")
            void shouldRemoveOnclickHandlers() {
                String result = markdownService.toHtml("<a href=\"#\" onclick=\"alert('xss')\">Link</a>");

                assertThat(result).doesNotContain("onclick");
            }

            @Test
            @DisplayName("should allow basic HTML tags")
            void shouldAllowBasicHtmlTags() {
                String result = markdownService.toHtml("**bold** and <em>italic</em>");

                assertThat(result).contains("<strong>");
                assertThat(result).contains("<em>");
            }

            @Test
            @DisplayName("should remove dangerous href attributes")
            void shouldRemoveDangerousHrefAttributes() {
                String result = markdownService.toHtml("[Link](javascript:alert('xss'))");

                assertThat(result).doesNotContain("javascript:");
            }
        }
    }
}
