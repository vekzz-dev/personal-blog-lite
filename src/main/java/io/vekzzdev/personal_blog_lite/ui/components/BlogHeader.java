package io.vekzzdev.personal_blog_lite.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;

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

        Button adminButton = new Button("Admin", VaadinIcon.LOCK.create());
        adminButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        adminButton.addClassName("blog-header__admin");
        adminButton.addClickListener(e -> {
            adminButton.getUI().ifPresent(ui -> {
                // Forzar redirección externa para activar BasicAuthFilter
                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                String contextPath = request.getContextPath();
                String adminUrl = contextPath + "/admin/dashboard";
                ui.getPage().setLocation(adminUrl);
            });
        });

        add(branding, adminButton);
    }
}
