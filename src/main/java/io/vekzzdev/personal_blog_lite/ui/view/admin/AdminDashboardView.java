package io.vekzzdev.personal_blog_lite.ui.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.vekzzdev.personal_blog_lite.exception.DomainException;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.service.PostService;
import io.vekzzdev.personal_blog_lite.ui.view.HomeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Route("admin/dashboard")
@PageTitle("Dashboard | Personal Blog")
@CssImport("./styles/admin-dashboard.css")
public class AdminDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardView.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PostService service;

    public AdminDashboardView(PostService service) {
        this.service = service;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();
        buildDashboard();
    }

    private void buildDashboard() {
        // === Header ===
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("admin-dashboard__header");
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout headerLeft = new HorizontalLayout();
        headerLeft.setAlignItems(Alignment.CENTER);
        headerLeft.setSpacing(true);

        Button backButton = new Button("Back to site", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate(HomeView.class)));

        H2 title = new H2("Dashboard");
        title.addClassName("admin-dashboard__title");

        headerLeft.add(backButton, title);

        Button newPostButton = new Button("New Post", VaadinIcon.PLUS.create());
        newPostButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newPostButton.addClickListener(e ->
                newPostButton.getUI().ifPresent(ui -> ui.navigate(NewPostView.class)));
        
        header.add(headerLeft, newPostButton);

        // === Grid de posts ===
        Grid<Post> grid = new Grid<>(Post.class, false);
        grid.addClassName("admin-dashboard__grid");
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Post::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(Post::getTitle)
                .setHeader("Title")
                .setFlexGrow(2);

        grid.addColumn(post -> post.getCreatedAt().format(DATE_FORMAT))
                .setHeader("Created")
                .setFlexGrow(1);

        grid.addColumn(post -> post.getUpdatedAt().format(DATE_FORMAT))
                .setHeader("Updated")
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        try {
            grid.setItems(service.getAllPosts());
        } catch (DomainException e) {
            log.error("Error loading posts for dashboard", e);
            Notification.show("Error loading posts")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        add(header, grid);
        setFlexGrow(1, grid);
    }

    private HorizontalLayout createActions(Post post) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(Alignment.CENTER);

        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
        editButton.addClickListener(e ->
                editButton.getUI().ifPresent(ui ->
                        ui.navigate(PostFormView.class, post.getId())));

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.addClickListener(e -> showDeleteConfirmation(post));

        actions.add(editButton, deleteButton);
        return actions;
    }

    private void showDeleteConfirmation(Post post) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete post");
        dialog.setText(
                "Are you sure you want to delete \"" + post.getTitle() + "\"? "
                        + "This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> deletePost(post));
        dialog.open();
    }

    private void deletePost(Post post) {
        try {
            service.deletePost(post.getId());
            Notification.show("Post deleted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(AdminDashboardView.class));
        } catch (DomainException e) {
            log.error("Error deleting post: id={}", post.getId(), e);
            Notification.show("Error deleting post: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
