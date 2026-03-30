package io.vekzzdev.personal_blog_lite.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;


public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = parser.parse(markdown);
        String rawHtml = renderer.render(document);

        return Jsoup.clean(rawHtml, Safelist.basic());
    }
}
