package dev.wardedlock.common.error;

import dev.wardedlock.common.error.exception.AppException;
import dev.wardedlock.common.error.exception.NotFoundException;
import dev.wardedlock.common.error.exception.RateLimitException;
import dev.wardedlock.common.error.handler.ErrorEnvelopeFactory;
import dev.wardedlock.common.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@Mock
	private ErrorEnvelopeFactory factory;

	private GlobalExceptionHandler handler;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler(factory);
		mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
			.setControllerAdvice(handler)
			.build();
	}

	@Test
	void shouldReturnCorrectStatusAndBodyForAppException() {
		ErrorEnvelope envelope = new ErrorEnvelope("NOT_FOUND", "Not Found", null, null, "url", Collections.emptyList());
		when(factory.create(any(AppException.class))).thenReturn(envelope);

		ResponseEntity<ErrorResponse> response = handler.handleAppException(NotFoundException.resourceNotFound("User", "1"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().error().code()).isEqualTo("NOT_FOUND");
	}

	@Test
	void shouldIncludeRetryAfterHeaderForRateLimit() {
		ErrorEnvelope envelope = new ErrorEnvelope("RATE_LIMIT_EXCEEDED", "Too Busy", null, null, "url", Collections.emptyList());
		when(factory.create(any(AppException.class))).thenReturn(envelope);

		RateLimitException ex = RateLimitException.exceeded(Duration.ofSeconds(120));
		ResponseEntity<ErrorResponse> response = handler.handleAppException(ex);

		assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("120");
	}

	@Test
	void shouldNotIncludeRetryAfterHeaderIfNull() {
		ErrorEnvelope envelope = new ErrorEnvelope("RATE_LIMIT_EXCEEDED", "Too Busy", null, null, "url", Collections.emptyList());
		when(factory.create(any(AppException.class))).thenReturn(envelope);

		RateLimitException ex = RateLimitException.exceeded(null);
		ResponseEntity<ErrorResponse> response = handler.handleAppException(ex);

		assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isNull();
	}

	@Test
	void handleThrowableShouldReturnInternalError() {
		ErrorEnvelope envelope = new ErrorEnvelope("INTERNAL_ERROR", "Internal Error", null, null, "url", Collections.emptyList());
		when(factory.create(any(Throwable.class))).thenReturn(envelope);

		ResponseEntity<ErrorResponse> response = handler.handleAny(new RuntimeException("Crash"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
	}

	@Test
	void shouldHandleMethodArgumentNotValidException() {
		ErrorEnvelope envelope = new ErrorEnvelope("VALIDATION_FAILED", "Validation failed", null, null, "url", 
			List.of(new ErrorField("field", "required", "message")));
		when(factory.create(any(MethodArgumentNotValidException.class))).thenReturn(envelope);

		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
		assertThat(response.getBody().error().fields()).isNotEmpty();
	}

	@Test
	void shouldHandleConstraintViolationException() {
		ErrorEnvelope envelope = new ErrorEnvelope("VALIDATION_FAILED", "Validation failed", null, null, "url", Collections.emptyList());
		when(factory.create(any(jakarta.validation.ConstraintViolationException.class))).thenReturn(envelope);

		jakarta.validation.ConstraintViolationException ex = new jakarta.validation.ConstraintViolationException(Collections.emptySet());
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void shouldHandleHttpMessageNotReadableException() {
		ErrorEnvelope envelope = new ErrorEnvelope("VALIDATION_FORMAT_INVALID", "Invalid format", null, null, "url", Collections.emptyList());
		when(factory.create(any(HttpMessageNotReadableException.class))).thenReturn(envelope);

		HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FORMAT_INVALID");
	}

	@Test
	void shouldHandleMethodArgumentTypeMismatchException() {
		ErrorEnvelope envelope = new ErrorEnvelope("VALIDATION_FORMAT_INVALID", "Invalid format", null, null, "url", Collections.emptyList());
		when(factory.create(any(MethodArgumentTypeMismatchException.class))).thenReturn(envelope);

		MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FORMAT_INVALID");
	}

	@Test
	void shouldHandleAccessDeniedException() {
		ErrorEnvelope envelope = new ErrorEnvelope("FORBIDDEN_INSUFFICIENT_SCOPE", "Forbidden", null, null, "url", Collections.emptyList());
		when(factory.create(any(AccessDeniedException.class))).thenReturn(envelope);

		AccessDeniedException ex = new AccessDeniedException("denied");
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody().error().code()).isEqualTo("FORBIDDEN_INSUFFICIENT_SCOPE");
	}

	@Test
	void shouldHandleAuthenticationException() {
		ErrorEnvelope envelope = new ErrorEnvelope("AUTH_INVALID_CREDENTIALS", "Unauthorized", null, null, "url", Collections.emptyList());
		when(factory.create(any(BadCredentialsException.class))).thenReturn(envelope);

		BadCredentialsException ex = new BadCredentialsException("bad");
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody().error().code()).isEqualTo("AUTH_INVALID_CREDENTIALS");
	}

	@Test
	void shouldHandleResponseStatusException() {
		ErrorEnvelope envelope = new ErrorEnvelope("NOT_FOUND", "Not Found", null, null, "url", Collections.emptyList());
		when(factory.create(any(ResponseStatusException.class))).thenReturn(envelope);

		ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "missing");
		ResponseEntity<ErrorResponse> response = handler.handleAny(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().error().code()).isEqualTo("NOT_FOUND");
	}

	@Test
	void shouldNotLeakStackTraceOrInternalDetailsInResponse() throws Exception {
		// Given: factory returning generic INTERNAL_ERROR with NO leak details
		ErrorEnvelope envelope = new ErrorEnvelope("INTERNAL_ERROR", "An internal error occurred", "req-1", "trace-1", "url", Collections.emptyList());
		when(factory.create(any(Throwable.class))).thenReturn(envelope);

		// When: triggered via a stub controller endpoint
		mockMvc.perform(get("/test/throw-wrapped"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
			// Then: response body must NOT contain any of these leak markers
			.andExpect(content().string(not(containsString("dev.wardedlock"))))
			.andExpect(content().string(not(containsString(".java:"))))
			.andExpect(content().string(not(containsString("Caused by:"))))
			.andExpect(content().string(not(matchesPattern(".*\\w+Exception.*"))))
			// Bonus: positive assertion that response is generic
			.andExpect(jsonPath("$.error.message").value(not(containsString("IllegalStateException"))));
	}

	@RestController
	@RequestMapping("/test")
	static class ThrowingController {
		@GetMapping("/throw-wrapped")
		public void throwWrapped() {
			throw new RuntimeException("Outer wrapper",
				new IllegalStateException("internal cause from dev.wardedlock.service.UserService"));
		}
	}
}
