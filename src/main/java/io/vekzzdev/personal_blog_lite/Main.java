package io.vekzzdev.personal_blog_lite;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting personal-blog-lite...");
        new VaadinBoot().run();
    }
}
