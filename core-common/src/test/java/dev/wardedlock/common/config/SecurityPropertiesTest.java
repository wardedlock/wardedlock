package dev.wardedlock.common.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityPropertiesTest {

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class)
    class DefaultValuesTest {
        @Autowired
        private SecurityProperties props;

        @Test
        void shouldBindDefaultValues() {
            assertThat(props.jwt().issuer()).isEqualTo("http://localhost:8080");
            assertThat(props.jwt().audience()).isEqualTo("wardedlock");
            assertThat(props.jwt().accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
            assertThat(props.jwt().refreshTokenTtl()).isEqualTo(Duration.ofDays(14));
            assertThat(props.jwt().jwksCacheTtl()).isEqualTo(Duration.ofMinutes(10));
            assertThat(props.jwt().activeKid()).isEqualTo("default");

            assertThat(props.argon2().memoryKb()).isEqualTo(65536);
            assertThat(props.argon2().iterations()).isEqualTo(3);
            assertThat(props.argon2().parallelism()).isEqualTo(1);

            assertThat(props.hmac().signatureToleranceSeconds()).isEqualTo(300);
        }
    }

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class, properties = "wardedlock.security.argon2.memoryKb=32768")
    class OverrideTest {
        @Autowired
        private SecurityProperties props;

        @Test
        void shouldOverrideValues() {
            assertThat(props.argon2().memoryKb()).isEqualTo(32768);
        }
    }

    @Nested
    class ValidationTest {
        @Test
        void shouldFailValidationForInvalidInput() {
            SpringApplication app = new SpringApplication(CommonConfigAutoConfiguration.class);
            app.setWebApplicationType(WebApplicationType.NONE);

            assertThatThrownBy(() -> app.run("--wardedlock.security.argon2.memory-kb=1024"))
                    .hasRootCauseInstanceOf(org.springframework.boot.context.properties.bind.validation.BindValidationException.class);
        }
    }
}
