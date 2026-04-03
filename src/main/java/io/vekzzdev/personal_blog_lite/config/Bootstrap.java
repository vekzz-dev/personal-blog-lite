package io.vekzzdev.personal_blog_lite.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vekzzdev.personal_blog_lite.repository.jooq.JooqPostRepository;
import io.vekzzdev.personal_blog_lite.service.MarkdownService;
import io.vekzzdev.personal_blog_lite.service.PostService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        hikariConfig.setJdbcUrl(getRequiredEnv("DB_URL"));
        hikariConfig.setUsername(getRequiredEnv("DB_USER"));
        hikariConfig.setPassword(getRequiredEnv("DB_PASSWORD"));
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
        
        try {
            flyway.migrate();
            log.info("Flyway migrations applied successfully");
        } catch (org.flywaydb.core.api.FlywayException e) {
            if (e.getMessage() != null && (e.getMessage().contains("Checksum mismatch") || e.getMessage().contains("checksum mismatch"))) {
                log.warn("Flyway checksum mismatch detected. Attempting repair...");
                try {
                    flyway.repair();
                    log.info("Flyway repair completed successfully");
                    
                    // Try migration again after repair
                    flyway.migrate();
                    log.info("Flyway migrations applied successfully after repair");
                } catch (Exception repairEx) {
                    log.error("Flyway repair failed: {}", repairEx.getMessage());
                    throw new RuntimeException("Failed to repair Flyway checksum mismatch", repairEx);
                }
            } else {
                log.error("Flyway migration failed: {}", e.getMessage());
                throw new RuntimeException("Failed to apply Flyway migrations", e);
            }
        }

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

    private static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable '" + key + "' is not set or is empty");
        }
        return value;
    }
}
