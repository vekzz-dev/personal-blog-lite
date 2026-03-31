package io.vekzzdev.personal_blog_lite.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vekzzdev.personal_blog_lite.repository.jooq.JooqPostRepository;
import io.vekzzdev.personal_blog_lite.service.MarkdownService;
import io.vekzzdev.personal_blog_lite.service.PostService;

import javax.sql.DataSource;

@WebListener
public class Bootstrap implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private static HikariDataSource dataSource;
    private static PostService postService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing personal-blog-lite...");

        // 1. HikariCP connection pool
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getEnvOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/blog_lite"));
        hikariConfig.setUsername(getEnvOrDefault("DB_USER", "blog_user"));
        hikariConfig.setPassword(getEnvOrDefault("DB_PASSWORD", "blog_pass"));
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setPoolName("blog-pool");

        dataSource = new HikariDataSource(hikariConfig);
        log.info("HikariCP connection pool initialized");

        // 2. Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        log.info("Flyway migrations applied successfully");

        // 3. Application services (composition root)
        postService = new PostService(
                new JooqPostRepository(getDslContext()),
                new MarkdownService()
        );
        log.info("Application services initialized");

        log.info("personal-blog-lite initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP connection pool closed");
        }
        log.info("personal-blog-lite shut down");
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized. Is Bootstrap running?");
        }
        return dataSource;
    }

    public static DSLContext getDslContext() {
        return DSL.using(getDataSource(), SQLDialect.MARIADB);
    }

    public static PostService getPostService() {
        if (postService == null) {
            throw new IllegalStateException("PostService not initialized. Is Bootstrap running?");
        }
        return postService;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
