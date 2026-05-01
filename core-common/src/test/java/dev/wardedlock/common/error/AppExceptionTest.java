package dev.wardedlock.common.error;

import dev.wardedlock.common.error.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AppExceptionTest {

	@Test
	void authExceptionShouldProduceCorrectCodes() {
		assertThat(AuthException.invalidCredentials().getErrorCode()).isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
		assertThat(AuthException.tokenExpired().getErrorCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
	}

	@Test
	void conflictExceptionShouldProduceCorrectCodesAndContext() {
		ConflictException ex = ConflictException.duplicate("User");
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT_DUPLICATE);
		assertThat(ex.getContext()).containsEntry("resource", "User");
	}

	@Test
	void notFoundExceptionShouldProduceCorrectCodesAndContext() {
		NotFoundException ex = NotFoundException.resourceNotFound("Order", "123");
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
		assertThat(ex.getContext()).containsEntry("resource", "Order");
		assertThat(ex.getContext()).containsEntry("id", "123");
	}

	@Test
	void rateLimitExceptionShouldIncludeRetryAfter() {
		Duration retryAfter = Duration.ofSeconds(60);
		RateLimitException ex = RateLimitException.exceeded(retryAfter);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
		assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
	}

	@Test
	void validationExceptionShouldIncludeFields() {
		List<ErrorField> fields = List.of(new ErrorField("email", "invalid", "Invalid email"));
		ValidationException ex = ValidationException.failed(fields);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
		assertThat(ex.getFields()).hasSize(1);
		assertThat(ex.getFields().get(0).path()).isEqualTo("email");
	}
}
