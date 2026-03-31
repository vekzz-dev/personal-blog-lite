package io.vekzzdev.personal_blog_lite.ui.view;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.service.PostService;
import io.vekzzdev.personal_blog_lite.ui.components.BlogHeader;

import java.time.format.DateTimeFormatter;

@Route("/home")
@PageTitle("Home | Personal Blog")
@CssImport("./styles/home-view.css")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final PostService service;

    public HomeView(PostService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();

        BlogHeader header = new BlogHeader();
        add(header);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        H2 sectionTitle = new H2("Latest Articles");
        sectionTitle.addClassName("home-view__section-title");
        content.add(sectionTitle);

        var posts = service.getAllPosts();

        if (posts.isEmpty()) {
            content.add(createEmptyState());
            content.setAlignItems(Alignment.CENTER);
        } else {
            VerticalLayout cardsContainer = new VerticalLayout();
            cardsContainer.setPadding(false);
            cardsContainer.setSpacing(true);
            cardsContainer.setWidth("100%");
            cardsContainer.setMaxWidth("800px");
            cardsContainer.setAlignItems(Alignment.STRETCH);

            for (Post post : posts) {
                cardsContainer.add(createPostCard(post));
            }

            Scroller scroller = new Scroller(cardsContainer);
            scroller.setSizeFull();
            content.addAndExpand(scroller);
        }

        addAndExpand(content);
    }

    private Card createPostCard(Post post) {
        Card card = new Card();
        card.addThemeVariants(CardVariant.ELEVATED);
        card.addClassName("post-card");
        card.setWidth("100%");

        Div cardTitle = new Div(post.getTitle());
        cardTitle.addClassName("post-card__title");
        card.setTitle(cardTitle);

        String formattedDate = post.getCreatedAt().format(DATE_FORMAT);
        Div dateLabel = new Div(formattedDate);
        dateLabel.addClassName("post-card__date");
        card.setSubtitle(dateLabel);

        if (post.getContent() != null && !post.getContent().isBlank()) {
            String preview = extractPreview(post.getContent());
            Span contentPreview = new Span(preview);
            contentPreview.addClassName("post-card__preview");
            card.add(contentPreview);
        }

        HorizontalLayout footer = new HorizontalLayout();
        footer.setAlignItems(Alignment.CENTER);
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.setWidthFull();

        Span readMore = new Span("Read more");
        readMore.addClassName("post-card__read-more");

        var arrowIcon = VaadinIcon.ARROW_RIGHT.create();
        arrowIcon.addClassName("post-card__arrow");

        footer.add(readMore, arrowIcon);
        card.addToFooter(footer);

        card.getElement().addEventListener("click", event ->
                card.getUI().ifPresent(ui ->
                        ui.navigate(PostDetailView.class, post.getId())));

        return card;
    }

    private String extractPreview(String content) {
        int maxLen = 150;
        String plain = content.lines()
                .filter(line -> !line.startsWith("#"))
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .findFirst()
                .orElse(content);
        if (plain.length() > maxLen) {
            return plain.substring(0, maxLen) + "...";
        }
        return plain;
    }

    private VerticalLayout createEmptyState() {
        VerticalLayout empty = new VerticalLayout();
        empty.addClassName("empty-state");
        empty.setSizeFull();
        empty.setSpacing(false);

        var icon = VaadinIcon.NOTEBOOK.create();
        icon.addClassName("empty-state__icon");

        Span message = new Span("No articles yet");
        message.addClassName("empty-state__message");

        Span hint = new Span("Check back soon for new content!");
        hint.addClassName("empty-state__hint");

        empty.add(icon, message, hint);
        return empty;
    }
}
