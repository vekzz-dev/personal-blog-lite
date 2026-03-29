package io.vekzzdev.personal_blog_lite.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("Hello world!"));
    }
}

