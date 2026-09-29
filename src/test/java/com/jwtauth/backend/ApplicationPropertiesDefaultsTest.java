package com.jwtauth.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ApplicationPropertiesDefaultsTest {

    @Autowired
    private Environment environment;

    @Test
    void defaultPropertiesShouldProvideLocalFallbacks() {
        assertThat(environment.getProperty("spring.datasource.url"))
                .contains("jdbc:h2:mem:");
        assertThat(environment.getProperty("spring.datasource.username"))
                .isEqualTo("sa");
        assertThat(environment.getProperty("app.jwt.secret"))
                .isNotBlank();
        assertThat(environment.getProperty("app.cors.allowed-origins"))
                .contains("localhost");
    }
}
