package io.vekzzdev.personal_blog_lite.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport("./styles/blog-header.css")
public class BlogHeader extends HorizontalLayout {

    public BlogHeader() {

        addClassName("blog-header");

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout branding = new HorizontalLayout();
        branding.setAlignItems(Alignment.CENTER);
        branding.setSpacing(true);

        var bookIcon = VaadinIcon.BOOK.create();
        bookIcon.addClassName("blog-header__icon");

        H1 title = new H1("Personal Blog");
        title.addClassName("blog-header__title");

        branding.add(bookIcon, title);

        Button signIn = new Button("Sign in", VaadinIcon.SIGN_IN.create());
        signIn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signIn.addClassName("blog-header__sign-in");
        signIn.addClickListener(e -> {
            Notification.show("Sign in clicked")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        add(branding, signIn);
    }
}
