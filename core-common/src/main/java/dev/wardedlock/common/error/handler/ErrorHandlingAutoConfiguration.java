package dev.wardedlock.common.error.handler;

import dev.wardedlock.common.config.PlatformProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for common error handling infrastructure.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(PlatformProperties.class)
public class ErrorHandlingAutoConfiguration {

	@Bean
	public ErrorEnvelopeFactory errorEnvelopeFactory(MessageSource messageSource, PlatformProperties platformProperties) {
		return new ErrorEnvelopeFactory(messageSource, platformProperties);
	}

	@Bean
	public GlobalExceptionHandler globalExceptionHandler(ErrorEnvelopeFactory errorEnvelopeFactory) {
		return new GlobalExceptionHandler(errorEnvelopeFactory);
	}
}
