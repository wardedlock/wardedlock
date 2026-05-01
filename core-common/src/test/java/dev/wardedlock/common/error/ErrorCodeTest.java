package dev.wardedlock.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

	@Test
	void shouldHaveCorrectMappingsForAllConstants() {
		for (ErrorCode code : ErrorCode.values()) {
			assertThat(code.getStatus()).isNotNull();
			assertThat(code.getMessageKey()).startsWith("error.");
			assertThat(code.getDocSlug()).doesNotContain("_");
		}
	}

	@Test
	void shouldMapToCorrectHttpStatus() {
		assertThat(ErrorCode.VALIDATION_FAILED.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.AUTH_INVALID_CREDENTIALS.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(ErrorCode.AUTH_LOCKED.getStatus()).isEqualTo(HttpStatus.LOCKED);
		assertThat(ErrorCode.FORBIDDEN_INSUFFICIENT_SCOPE.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(ErrorCode.NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ErrorCode.CONFLICT_DUPLICATE.getStatus()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.RATE_LIMIT_EXCEEDED.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(ErrorCode.UPSTREAM_TIMEOUT.getStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
		assertThat(ErrorCode.IDEMPOTENCY_REPLAY.getStatus()).isEqualTo(HttpStatus.OK);
		assertThat(ErrorCode.INTERNAL_ERROR.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
