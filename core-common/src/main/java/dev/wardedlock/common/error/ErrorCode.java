package dev.wardedlock.common.error;

import org.springframework.http.HttpStatus;

/**
 * Global error codes for the Wardedlock platform.
 * Each code maps to an HTTP status, a message bundle key, and a documentation slug.
 */
public enum ErrorCode {
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
	VALIDATION_FIELD_REQUIRED(HttpStatus.BAD_REQUEST),
	VALIDATION_FORMAT_INVALID(HttpStatus.BAD_REQUEST),
	AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
	AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
	AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
	AUTH_MFA_REQUIRED(HttpStatus.UNAUTHORIZED),
	AUTH_LOCKED(HttpStatus.LOCKED),
	FORBIDDEN_INSUFFICIENT_SCOPE(HttpStatus.FORBIDDEN),
	FORBIDDEN_TENANT_MISMATCH(HttpStatus.FORBIDDEN),
	NOT_FOUND(HttpStatus.NOT_FOUND),
	CONFLICT_DUPLICATE(HttpStatus.CONFLICT),
	CONFLICT_VERSION_MISMATCH(HttpStatus.CONFLICT),
	RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
	UPSTREAM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT),
	UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
	UPSTREAM_BAD_RESPONSE(HttpStatus.BAD_GATEWAY),
	IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT),
	IDEMPOTENCY_REPLAY(HttpStatus.OK),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

	private final HttpStatus status;

	ErrorCode(HttpStatus status) {
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getMessageKey() {
		return "error." + name().toLowerCase();
	}

	public String getDocSlug() {
		return name().toLowerCase().replace('_', '-');
	}
}
