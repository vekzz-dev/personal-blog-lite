package io.vekzzdev.personal_blog_lite;

import com.github.mvysny.vaadinboot.VaadinBoot;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@StyleSheet(Lumo.STYLESHEET)
public class Main implements AppShellConfigurator {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting personal-blog-lite...");
        new VaadinBoot().run();
    }
}
