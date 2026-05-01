package dev.wardedlock.common.error;

import dev.wardedlock.common.error.handler.ErrorEnvelopeFactory;
import dev.wardedlock.common.error.handler.ErrorHandlingAutoConfiguration;
import dev.wardedlock.common.error.handler.GlobalExceptionHandler;
import dev.wardedlock.common.config.PlatformProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlingAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
		.withPropertyValues("wardedlock.platform.docs.base-url=https://docs.test.com");

	@Test
	void shouldLoadBeansInWebContext() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
			assertThat(context).hasSingleBean(ErrorEnvelopeFactory.class);
			assertThat(context).hasSingleBean(MessageSource.class);
		});
	}

	@Test
	void shouldNotLoadInNonWebContext() {
		new org.springframework.boot.test.context.runner.ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
			.run(context -> {
				assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class);
			});
	}
}
