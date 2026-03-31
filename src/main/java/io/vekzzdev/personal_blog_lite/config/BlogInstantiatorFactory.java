package io.vekzzdev.personal_blog_lite.config;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import io.vekzzdev.personal_blog_lite.ui.view.HomeView;
import io.vekzzdev.personal_blog_lite.ui.view.PostDetailView;

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
            return super.getOrCreate(type);
        }
    }
}
