package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.ErrorField;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for all domain-specific application exceptions.
 * Encapsulates error codes, context, and optional retry information.
 */
public abstract class AppException extends RuntimeException {

	private final ErrorCode errorCode;
	private final Map<String, Object> context;
	private final List<ErrorField> fields;
	private final Duration retryAfter;

	protected AppException(
		ErrorCode errorCode,
		Map<String, Object> context,
		List<ErrorField> fields,
		Duration retryAfter,
		Throwable cause
	) {
		super(errorCode.name(), cause);
		this.errorCode = errorCode;
		this.context = context != null ? Map.copyOf(context) : Collections.emptyMap();
		this.fields = fields != null ? List.copyOf(fields) : Collections.emptyList();
		this.retryAfter = retryAfter;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public List<ErrorField> getFields() {
		return fields;
	}

	public Duration getRetryAfter() {
		return retryAfter;
	}

	public Object[] getMessageArgs() {
		return new Object[0];
	}
}
