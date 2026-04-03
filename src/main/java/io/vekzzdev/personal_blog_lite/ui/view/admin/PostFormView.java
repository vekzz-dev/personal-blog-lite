package io.vekzzdev.personal_blog_lite.ui.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.vekzzdev.personal_blog_lite.exception.DomainException;
import io.vekzzdev.personal_blog_lite.model.Post;
import io.vekzzdev.personal_blog_lite.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("admin/post")
@PageTitle("Post Form | Personal Blog")
@CssImport("./styles/admin-post-form.css")
public class PostFormView extends VerticalLayout implements HasUrlParameter<Integer> {

    private static final Logger log = LoggerFactory.getLogger(PostFormView.class);

    private final PostService service;

    private TextField titleField;
    private TextArea contentArea;
    private boolean isEditMode = false;
    private int editPostId = -1;

    public PostFormView(PostService service) {
        this.service = service;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer postId) {
        log.info("PostFormView.setParameter called with postId: {}", postId);
        removeAll();

        if (postId != null && postId > 0) {
            log.info("Entering edit mode for post id: {}", postId);
            isEditMode = true;
            editPostId = postId;
            loadPost(postId);
        } else {
            log.info("Entering new post creation mode");
            isEditMode = false;
            buildForm(null);
        }
    }

    private void loadPost(int postId) {
        try {
            Post post = service.getPostById(postId);
            buildForm(post);
        } catch (DomainException e) {
            log.error("Error loading post for editing: id={}", postId, e);
            Notification.show("Post not found")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(AdminDashboardView.class));
        }
    }

    private void buildForm(Post existingPost) {
        // === Header ===
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("admin-post-form__header");
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout headerLeft = new HorizontalLayout();
        headerLeft.setAlignItems(Alignment.CENTER);
        headerLeft.setSpacing(true);

        Button backButton = new Button("Back to dashboard", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate(AdminDashboardView.class)));

        String pageTitle = isEditMode ? "Edit Post" : "New Post";
        H2 title = new H2(pageTitle);
        title.addClassName("admin-post-form__title");

        headerLeft.add(backButton, title);
        header.add(headerLeft);

        // === Form ===
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.addClassName("admin-post-form__container");
        formContainer.setSizeFull();
        formContainer.setPadding(true);
        formContainer.setMaxWidth("800px");
        formContainer.setAlignItems(Alignment.STRETCH);

        titleField = new TextField("Title");
        titleField.addClassName("admin-post-form__title-field");
        titleField.setPlaceholder("Enter post title...");
        titleField.setRequired(true);
        titleField.setMaxLength(255);
        titleField.setWidthFull();

        contentArea = new TextArea("Content (Markdown)");
        contentArea.addClassName("admin-post-form__content-area");
        contentArea.setPlaceholder("Write your post content in Markdown...");
        contentArea.setRequired(true);
        contentArea.setWidthFull();
        contentArea.setHeight("400px");

        // Pre-poblar si es edición
        if (existingPost != null) {
            titleField.setValue(existingPost.getTitle() != null ? existingPost.getTitle() : "");
            contentArea.setValue(existingPost.getContent() != null ? existingPost.getContent() : "");
        } else {
            titleField.setValue("");
            contentArea.setValue("");
        }

        // === Actions ===
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("admin-post-form__actions");
        actions.setSpacing(true);

        Button saveButton = new Button(
                isEditMode ? "Update Post" : "Create Post",
                VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> savePost());

        Button cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e ->
                cancelButton.getUI().ifPresent(ui -> ui.navigate(AdminDashboardView.class)));

        actions.add(saveButton, cancelButton);

        formContainer.add(titleField, contentArea, actions);
        add(header, formContainer);
        setFlexGrow(1, formContainer);
    }

    private void savePost() {
        String title = titleField.getValue().trim();
        String content = contentArea.getValue().trim();

        // Validación básica
        if (title.isEmpty()) {
            Notification.show("Title is required")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            titleField.focus();
            return;
        }

        if (content.isEmpty()) {
            Notification.show("Content is required")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            contentArea.focus();
            return;
        }

        try {
            if (isEditMode) {
                service.updatePost(editPostId, title, content);
                log.info("Post updated: id={}", editPostId);
                Notification.show("Post updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                service.createPost(title, content);
                log.info("Post created: title={}", title);
                Notification.show("Post created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            getUI().ifPresent(ui -> ui.navigate(AdminDashboardView.class));
        } catch (DomainException e) {
            log.error("Error saving post", e);
            Notification.show("Error saving post: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
