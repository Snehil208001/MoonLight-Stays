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
            // Fallback to individual DB variables (common on Azure App Service)
            String host = System.getenv("DB_HOST");
            if (host == null) host = System.getenv("RDS_HOSTNAME");
            if (host == null) host = System.getenv("PGHOST");

            if (host != null && !host.trim().isEmpty()) {
                String port = System.getenv("DB_PORT");
                if (port == null) port = System.getenv("RDS_PORT");
                if (port == null) port = System.getenv("PGPORT");
                if (port == null) port = "5432";

                String dbName = System.getenv("DB_NAME");
                if (dbName == null) dbName = System.getenv("RDS_DB_NAME");
                if (dbName == null) dbName = System.getenv("PGDATABASE");
                if (dbName == null) dbName = "airBnb";

                String username = System.getenv("DB_USERNAME");
                if (username == null) username = System.getenv("RDS_USERNAME");
                if (username == null) username = System.getenv("PGUSER");
                if (username == null) username = "postgres";

                String password = System.getenv("DB_PASSWORD");
                if (password == null) password = System.getenv("RDS_PASSWORD");
                if (password == null) password = System.getenv("PGPASSWORD");
                if (password == null) password = "";

                String dbUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                if (host != null && !host.equals("localhost") && !host.equals("127.0.0.1")) {
                    dbUrl += "?sslmode=require";
                }

                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(dbUrl);
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setDriverClassName("org.postgresql.Driver");

                return new HikariDataSource(hikariConfig);
            }

            throw new IllegalStateException("DATABASE_URL environment variable is missing, and no fallback DB_HOST/RDS_HOSTNAME variable was found.");
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
