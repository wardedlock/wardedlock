package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.time.Duration;
import java.util.Collections;

/**
 * Exception thrown when rate limits are exceeded.
 */
public final class RateLimitException extends AppException {

	private RateLimitException(ErrorCode errorCode, Duration retryAfter) {
		super(errorCode, Collections.emptyMap(), Collections.emptyList(), retryAfter, null);
	}

	public static RateLimitException exceeded(Duration retryAfter) {
		return new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED, retryAfter);
	}

	@Override
	public Object[] getMessageArgs() {
		return new Object[]{getRetryAfter() != null ? getRetryAfter().toSeconds() : 0};
	}
}
