package com.jwtauth.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseEnvironmentPostProcessorTest {

    @Test
    void shouldConfigureFromMysqlHost() {
        ConfigurableEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "MYSQLHOST", "mysql.railway.internal",
                "MYSQLPORT", "3306",
                "MYSQLDATABASE", "railway",
                "MYSQLUSER", "root",
                "MYSQLPASSWORD", "secret123"
        )));

        DatabaseEnvironmentPostProcessor processor = new DatabaseEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:mysql://mysql.railway.internal:3306/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("root");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secret123");
        assertThat(env.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    void shouldConfigureFromMysqlUrl() {
        ConfigurableEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "MYSQL_URL", "mysql://root:secretPass@mysql.railway.internal:3306/railway"
        )));

        DatabaseEnvironmentPostProcessor processor = new DatabaseEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:mysql://mysql.railway.internal:3306/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("root");
        assertThat(env.getProperty("spring.datasource.password")).isEqualTo("secretPass");
    }

    @Test
    void shouldDoNothingWhenNoDatabaseEnv() {
        ConfigurableEnvironment env = new StandardEnvironment();
        DatabaseEnvironmentPostProcessor processor = new DatabaseEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.datasource.url")).isNull();
    }
}
