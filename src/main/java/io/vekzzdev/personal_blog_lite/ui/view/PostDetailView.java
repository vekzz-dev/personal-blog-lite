package io.vekzzdev.personal_blog_lite.ui.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.vekzzdev.personal_blog_lite.exception.PostNotFoundException;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.service.PostService;
import io.vekzzdev.personal_blog_lite.ui.components.BlogHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Route("post")
@PageTitle("Post | Personal Blog")
@CssImport("./styles/post-detail-view.css")
public class PostDetailView extends VerticalLayout implements HasUrlParameter<Integer> {

    private static final Logger log = LoggerFactory.getLogger(PostDetailView.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final PostService service;

    public PostDetailView(PostService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer postId) {
        removeAll();

        BlogHeader header = new BlogHeader();
        add(header);

        try {
            Post post = service.getPostById(postId);
            renderPost(post);
        } catch (PostNotFoundException e) {
            log.warn("Post not found: id={}", postId);
            renderError("Article not found");
        } catch (Exception e) {
            log.error("Error loading post: id={}", postId, e);
            renderError("Something went wrong");
        }
    }

    private void renderPost(Post post) {
        VerticalLayout content = new VerticalLayout();
        content.addClassName("post-detail");
        content.setPadding(false);
        content.setSpacing(false);

        // Back button
        Button backButton = new Button("Back to articles", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClassName("post-detail__back");
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate(HomeView.class)));

        // Title
        H1 title = new H1(post.getTitle());
        title.addClassName("post-detail__title");

        // Meta: published + updated
        HorizontalLayout meta = new HorizontalLayout();
        meta.addClassName("post-detail__meta");
        meta.setAlignItems(Alignment.CENTER);
        meta.setSpacing(false);

        Span publishedLabel = createMetaItem(
                VaadinIcon.CALENDAR.create(),
                "Published " + post.getCreatedAt().format(DATE_FORMAT));
        meta.add(publishedLabel);

        if (post.getUpdatedAt() != null && !post.getUpdatedAt().equals(post.getCreatedAt())) {
            Span updatedLabel = createMetaItem(
                    VaadinIcon.REFRESH.create(),
                    "Updated " + post.getUpdatedAt().format(DATE_FORMAT));
            meta.add(updatedLabel);
        }

        // Separator
        Div separator = new Div();
        separator.addClassName("post-detail__divider");

        // Content rendered from markdown to HTML
        String htmlContent = service.renderContentPostToHtml(post.getContent());
        Html renderedContent = new Html(
                "<div class=\"post-detail__content\">" + htmlContent + "</div>");

        content.add(backButton, title, meta, separator, renderedContent);
        addAndExpand(content);
    }

    private Span createMetaItem(com.vaadin.flow.component.icon.Icon icon, String text) {
        Span item = new Span();
        item.addClassName("post-detail__meta-item");
        icon.getStyle().set("font-size", "0.875rem");
        Span label = new Span(text);
        item.add(icon, label);
        return item;
    }

    private void renderError(String message) {
        VerticalLayout error = new VerticalLayout();
        error.addClassName("post-detail__error");
        error.setSizeFull();
        error.setAlignItems(Alignment.CENTER);
        error.setJustifyContentMode(JustifyContentMode.CENTER);
        error.setSpacing(true);

        var icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        icon.addClassName("post-detail__error-icon");

        Span errorMessage = new Span(message);
        errorMessage.addClassName("post-detail__error-message");

        Button backButton = new Button("Go back home", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate(HomeView.class)));

        error.add(icon, errorMessage, backButton);
        addAndExpand(error);

        Notification.show(message)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
