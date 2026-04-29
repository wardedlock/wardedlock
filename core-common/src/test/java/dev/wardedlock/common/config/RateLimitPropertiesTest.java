package dev.wardedlock.common.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitPropertiesTest {

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class)
    class DefaultValuesTest {
        @Autowired
        private RateLimitProperties props;

        @Test
        void shouldBindDefaultValues() {
            assertThat(props.rules()).hasSize(4);
            assertThat(props.rules().get("/auth/login").limit()).isEqualTo(60);
            assertThat(props.rules().get("/auth/login").windowSeconds()).isEqualTo(60);
            assertThat(props.rules().get("/auth/login").keyStrategy()).isEqualTo(RateLimitProperties.KeyStrategy.IP);
        }
    }

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class, properties = "wardedlock.ratelimit.rules.[/auth/login].limit=10")
    class OverrideTest {
        @Autowired
        private RateLimitProperties props;

        @Test
        void shouldOverrideValues() {
            assertThat(props.rules().get("/auth/login").limit()).isEqualTo(10);
        }
    }

    @Nested
    @SpringBootTest(classes = CommonConfigAutoConfiguration.class, properties = {
            "wardedlock.ratelimit.rules.[/new/route].limit=5",
            "wardedlock.ratelimit.rules.[/new/route].window-seconds=10",
            "wardedlock.ratelimit.rules.[/new/route].key-strategy=IP"
    })
    class UnknownRouteTest {
        @Autowired
        private RateLimitProperties props;

        @Test
        void shouldAllowUnknownRouteKeys() {
            assertThat(props.rules()).containsKey("/new/route");
            assertThat(props.rules().get("/new/route").limit()).isEqualTo(5);
        }
    }

    @Nested
    class ValidationTest {
        @Test
        void shouldFailValidationForInvalidInput() {
            SpringApplication app = new SpringApplication(CommonConfigAutoConfiguration.class);
            app.setWebApplicationType(WebApplicationType.NONE);

            assertThatThrownBy(() -> app.run("--wardedlock.ratelimit.rules.[/auth/login].limit=-1"))
                    .hasRootCauseInstanceOf(org.springframework.boot.context.properties.bind.validation.BindValidationException.class);
        }
    }
}
