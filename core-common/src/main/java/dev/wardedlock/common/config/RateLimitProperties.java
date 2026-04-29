package dev.wardedlock.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "wardedlock.ratelimit")
public record RateLimitProperties(
        @NotNull Map<String, @Valid Rule> rules
) {
    public record Rule(
            @Positive int limit,
            @Positive int windowSeconds,
            @NotNull KeyStrategy keyStrategy
    ) {}

    public enum KeyStrategy {
        IP,
        USER,
        IDENTIFIER_PLUS_IP,
        CLIENT_ID
    }
}
