package dev.wardedlock.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "wardedlock.cache")
public record CacheProperties(
        @Valid @NotNull Ttl ttl,
        @Valid @NotNull Lettuce lettuce
) {
    public record Ttl(
            @NotNull @DurationMin(nanos = 0) Duration session,
            @NotNull @DurationMin(nanos = 0) Duration refreshToken,
            @NotNull @DurationMin(nanos = 0) Duration pkce,
            @NotNull @DurationMin(nanos = 0) Duration loginAttempts,
            @NotNull @DurationMin(nanos = 0) Duration userPermissions,
            @NotNull @DurationMin(nanos = 0) Duration idempotencyKey,
            @NotNull @DurationMin(nanos = 0) Duration jwksCache
    ) {}

    public record Lettuce(
            @NotNull Duration commandTimeout,
            @Positive int poolMaxActive,
            @PositiveOrZero int poolMaxIdle,
            @NotNull Duration shutdownTimeout
    ) {
        public Lettuce {
            if (poolMaxIdle > poolMaxActive) {
                throw new IllegalArgumentException("poolMaxIdle cannot be greater than poolMaxActive");
            }
        }
    }
}
