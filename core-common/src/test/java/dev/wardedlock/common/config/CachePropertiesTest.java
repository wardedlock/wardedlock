package dev.wardedlock.common.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CachePropertiesTest {

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class)
    class DefaultValuesTest {
        @Autowired
        private CacheProperties props;

        @Test
        void shouldBindDefaultValues() {
            assertThat(props.ttl().session()).isEqualTo(Duration.ofMinutes(30));
            assertThat(props.ttl().refreshToken()).isEqualTo(Duration.ofDays(14));
            assertThat(props.ttl().pkce()).isEqualTo(Duration.ofMinutes(10));
            assertThat(props.ttl().loginAttempts()).isEqualTo(Duration.ofMinutes(15));
            assertThat(props.ttl().userPermissions()).isEqualTo(Duration.ofMinutes(5));
            assertThat(props.ttl().idempotencyKey()).isEqualTo(Duration.ofHours(24));
            assertThat(props.ttl().jwksCache()).isEqualTo(Duration.ofMinutes(10));

            assertThat(props.lettuce().commandTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(props.lettuce().poolMaxActive()).isEqualTo(16);
            assertThat(props.lettuce().poolMaxIdle()).isEqualTo(8);
            assertThat(props.lettuce().shutdownTimeout()).isEqualTo(Duration.ofMillis(100));
        }
    }

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class, properties = "wardedlock.cache.ttl.session=1s")
    class OverrideTest {
        @Autowired
        private CacheProperties props;

        @Test
        void shouldOverrideValues() {
            assertThat(props.ttl().session()).isEqualTo(Duration.ofSeconds(1));
        }
    }

    @Nested
    class ValidationTest {
        @Test
        void shouldFailValidationForInvalidInput() {
            SpringApplication app = new SpringApplication(CommonConfigAutoConfiguration.class);
            app.setWebApplicationType(WebApplicationType.NONE);

            assertThatThrownBy(() -> app.run("--wardedlock.cache.ttl.session=-1s"))
                    .hasRootCauseInstanceOf(org.springframework.boot.context.properties.bind.validation.BindValidationException.class);
        }
    }
}
