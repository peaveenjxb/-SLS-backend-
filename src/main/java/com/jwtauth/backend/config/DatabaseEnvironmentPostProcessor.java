package com.jwtauth.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Automatically detects Railway MySQL environment variables (MYSQLHOST, MYSQL_URL, etc.)
 * or standard DB_* variables and configures Spring Boot's datasource and Hibernate dialect.
 * Falls back to local H2 in-memory database if no database environment is configured.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseEnvironmentPostProcessor.class);
    private static final String PROPERTY_SOURCE_NAME = "railwayDatabaseProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> props = new HashMap<>();

        String rawUrl = getFirstNonEmpty(environment, "MYSQL_URL", "MYSQL_PRIVATE_URL", "DATABASE_URL", "DB_URL");
        String mysqlHost = getFirstNonEmpty(environment, "MYSQLHOST", "DB_HOST");

        if (rawUrl != null && (rawUrl.startsWith("mysql://") || rawUrl.startsWith("mysql2://"))) {
            log.info("Detected MySQL connection URL from environment; converting to JDBC format");
            configureFromMysqlUrl(rawUrl, environment, props);
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:mysql:")) {
            log.info("Detected JDBC MySQL URL from DB_URL");
            props.put("spring.datasource.url", rawUrl);
            props.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
            props.put("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
            props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            copyIfPresent(environment, props, "DB_USERNAME", "spring.datasource.username");
            copyIfPresent(environment, props, "DB_PASSWORD", "spring.datasource.password");
        } else if (mysqlHost != null && !mysqlHost.isBlank()) {
            log.info("Detected Railway MYSQLHOST: {}", mysqlHost);
            String port = environment.getProperty("MYSQLPORT", "3306");
            String db = getFirstNonEmpty(environment, "MYSQLDATABASE", "DB_NAME");
            if (db == null || db.isBlank()) {
                db = "railway";
            }
            String user = getFirstNonEmpty(environment, "MYSQLUSER", "DB_USERNAME");
            if (user == null || user.isBlank()) {
                user = "root";
            }
            String password = getFirstNonEmpty(environment, "MYSQLPASSWORD", "DB_PASSWORD");
            if (password == null) {
                password = "";
            }

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    mysqlHost, port, db
            );

            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", user);
            props.put("spring.datasource.password", password);
            props.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
            props.put("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
            props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
            log.info("Configured datasource properties for Railway MySQL: url={}", props.get("spring.datasource.url"));
        }
    }

    private void configureFromMysqlUrl(String rawUrl, ConfigurableEnvironment environment, Map<String, Object> props) {
        try {
            // Strip mysql:// or mysql2:// for parsing
            String normalized = rawUrl.replaceFirst("^mysql2?://", "http://");
            URI uri = URI.create(normalized);

            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 3306;
            String path = uri.getPath();
            String db = (path != null && path.length() > 1) ? path.substring(1) : "railway";

            String user = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":", 2);
                user = parts[0];
                if (parts.length > 1) {
                    password = parts[1];
                }
            }

            if (user == null || user.isBlank()) {
                user = getFirstNonEmpty(environment, "MYSQLUSER", "DB_USERNAME");
                if (user == null || user.isBlank()) {
                    user = "root";
                }
            }
            if (password == null) {
                password = getFirstNonEmpty(environment, "MYSQLPASSWORD", "DB_PASSWORD");
                if (password == null) {
                    password = "";
                }
            }

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, db
            );

            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", user);
            props.put("spring.datasource.password", password);
            props.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
            props.put("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
            props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        } catch (Exception e) {
            log.warn("Failed to parse MySQL URL '{}', falling back to defaults: {}", rawUrl, e.getMessage());
        }
    }

    private String getFirstNonEmpty(ConfigurableEnvironment env, String... keys) {
        for (String key : keys) {
            String val = env.getProperty(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    private void copyIfPresent(ConfigurableEnvironment env, Map<String, Object> props, String envKey, String targetKey) {
        String val = env.getProperty(envKey);
        if (val != null && !val.isBlank()) {
            props.put(targetKey, val);
        }
    }
}
