package dev.wardedlock.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Platform-wide configuration properties.
 */
@Validated
@ConfigurationProperties(prefix = "wardedlock.platform")
public record PlatformProperties(
	@NotNull @Valid Docs docs
) {
	/**
	 * Documentation related properties.
	 */
	public record Docs(
		@NotBlank @URL String baseUrl
	) {}
}
