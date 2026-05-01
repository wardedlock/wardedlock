package dev.wardedlock.common.error;

import dev.wardedlock.common.config.PlatformProperties;
import dev.wardedlock.common.error.exception.NotFoundException;
import dev.wardedlock.common.error.handler.ErrorEnvelopeFactory;
import dev.wardedlock.common.keys.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorEnvelopeFactoryTest {

	@Mock
	private MessageSource messageSource;

	@Mock
	private PlatformProperties platformProperties;

	@Mock
	private PlatformProperties.Docs docs;

	private ErrorEnvelopeFactory factory;

	@BeforeEach
	void setUp() {
		when(platformProperties.docs()).thenReturn(docs);
		when(docs.baseUrl()).thenReturn("https://docs.wardedlock.dev");
		factory = new ErrorEnvelopeFactory(messageSource, platformProperties);
		MDC.clear();
	}

	@Test
	void shouldCreateEnvelopeFromAppException() {
		when(messageSource.getMessage(eq("error.not_found"), any(), any(Locale.class)))
			.thenReturn("Not found message");

		NotFoundException ex = NotFoundException.resourceNotFound("User", "123");
		ErrorEnvelope envelope = factory.create(ex);

		assertThat(envelope.code()).isEqualTo("NOT_FOUND");
		assertThat(envelope.message()).isEqualTo("Not found message");
		assertThat(envelope.docUrl()).isEqualTo("https://docs.wardedlock.dev/not-found");
	}

	@Test
	void shouldIncludeMdcIdsIfPresent() {
		MDC.put(HttpHeaders.Mdc.REQUEST_ID, "req-123");
		MDC.put(HttpHeaders.Mdc.TRACE_ID, "trace-456");

		ErrorEnvelope envelope = factory.create(new RuntimeException("Oops"));

		assertThat(envelope.requestId()).isEqualTo("req-123");
		assertThat(envelope.traceId()).isEqualTo("trace-456");
	}

	@Test
	void shouldFallbackToCodeNameIfMessageMissing() {
		when(messageSource.getMessage(any(), any(), any()))
			.thenThrow(new RuntimeException("Missing bundle"));

		ErrorEnvelope envelope = factory.create(new RuntimeException("Oops"));

		assertThat(envelope.message()).isEqualTo("INTERNAL_ERROR");
	}

	@Test
	void shouldNotLeakStacktraceInMessage() {
		RuntimeException inner = new RuntimeException("Inner secret");
		RuntimeException outer = new RuntimeException("Outer layer", inner);

		ErrorEnvelope envelope = factory.create(outer);

		assertThat(envelope.message()).isEqualTo("INTERNAL_ERROR");
		assertThat(envelope.message()).doesNotContain("Inner secret");
		assertThat(envelope.message()).doesNotContain("dev.wardedlock");
	}
}
