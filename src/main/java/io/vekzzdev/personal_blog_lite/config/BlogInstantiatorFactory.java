package io.vekzzdev.personal_blog_lite.config;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import io.vekzzdev.personal_blog_lite.ui.view.HomeView;
import io.vekzzdev.personal_blog_lite.ui.view.PostDetailView;
import io.vekzzdev.personal_blog_lite.ui.view.admin.AdminDashboardView;
import io.vekzzdev.personal_blog_lite.ui.view.admin.NewPostView;
import io.vekzzdev.personal_blog_lite.ui.view.admin.PostFormView;

/**
 * Custom Vaadin Instantiator that wires view dependencies via constructor injection.
 * Registered via META-INF/services/com.vaadin.flow.di.InstantiatorFactory.
 */
public class BlogInstantiatorFactory implements InstantiatorFactory {

    @Override
    public Instantiator createInstantitor(VaadinService service) {
        return new BlogInstantiator(service);
    }

    private static class BlogInstantiator extends DefaultInstantiator {

        BlogInstantiator(VaadinService service) {
            super(service);
        }

        @Override
        public <T> T getOrCreate(Class<T> type) {
            if (type == HomeView.class) {
                return type.cast(new HomeView(Bootstrap.getPostService()));
            }
            if (type == PostDetailView.class) {
                return type.cast(new PostDetailView(Bootstrap.getPostService()));
            }
            if (type == AdminDashboardView.class) {
                return type.cast(new AdminDashboardView(Bootstrap.getPostService()));
            }
            if (type == PostFormView.class) {
                return type.cast(new PostFormView(Bootstrap.getPostService()));
            }
            if (type == NewPostView.class) {
                return type.cast(new NewPostView(Bootstrap.getPostService()));
            }
            return super.getOrCreate(type);
        }
    }
}
