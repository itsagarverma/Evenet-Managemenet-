package com.sagar.eventmanagement.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Builds the database connection.
 *
 * Supports TWO formats so the same code works locally and on Render:
 *
 * 1. DATABASE_URL - a single combined connection string like Render/Supabase provide,
 *    e.g. postgresql://username:password@host:5432/dbname
 *    (Render sets this automatically when you attach a Postgres database - takes priority if present)
 *
 * 2. DB_URL / DB_USERNAME / DB_PASSWORD - separate values, used for local development,
 *    e.g. DB_URL=jdbc:postgresql://localhost:5432/eventdb
 */
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${DB_URL:jdbc:postgresql://localhost:5432/eventdb}")
    private String fallbackUrl;

    @Value("${DB_USERNAME:postgres}")
    private String fallbackUsername;

    @Value("${DB_PASSWORD:postgres}")
    private String fallbackPassword;

    @Bean
    public DataSource dataSource() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            // Render/Supabase-style combined URL: postgresql://user:password@host:5432/dbname
            URI uri = new URI(databaseUrl);
            String[] userInfo = uri.getUserInfo().split(":", 2);
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(userInfo[0]);
            config.setPassword(userInfo.length > 1 ? userInfo[1] : "");
        } else {
            // Local development
            config.setJdbcUrl(fallbackUrl);
            config.setUsername(fallbackUsername);
            config.setPassword(fallbackPassword);
        }

        return new HikariDataSource(config);
    }
}
