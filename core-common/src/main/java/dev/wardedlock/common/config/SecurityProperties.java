package dev.wardedlock.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "wardedlock.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Argon2 argon2,
        @Valid @NotNull Hmac hmac
) {
    public record Jwt(
            @NotBlank String issuer,
            @NotBlank String audience,
            @NotNull @DurationMin(seconds = 1) Duration accessTokenTtl,
            @NotNull @DurationMin(seconds = 1) Duration refreshTokenTtl,
            @NotNull @DurationMin(seconds = 1) Duration jwksCacheTtl,
            @NotBlank String activeKid
    ) {
        public Jwt {
            if (issuer != null && !issuer.isBlank()) {
                try {
                    URI.create(issuer);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("issuer must be a valid URI", e);
                }
            }
        }
    }

    public record Argon2(
            @Min(16384) int memoryKb,
            @Min(2) int iterations,
            @Min(1) int parallelism
    ) {}

    public record Hmac(
            @Min(30) @Max(900) int signatureToleranceSeconds
    ) {}
}
