package com.moonlight.project.airBnbApp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is missing. Please provide it in Railway variables.");
        }

        try {
            // Parse postgresql:// or postgres://
            String cleanedUrl = databaseUrl.replace("postgresql://", "postgres://");
            URI dbUri = new URI(cleanedUrl);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            
            // Build the JDBC URL
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(dbUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setDriverClassName("org.postgresql.Driver");

            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL environment variable: " + databaseUrl, e);
        }
    }
}
