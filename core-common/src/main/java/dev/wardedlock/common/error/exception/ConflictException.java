package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown for conflict failures (e.g. duplicates, version mismatch).
 */
public final class ConflictException extends AppException {

	private ConflictException(ErrorCode errorCode, Map<String, Object> context) {
		super(errorCode, context, Collections.emptyList(), null, null);
	}

	public static ConflictException duplicate(String resource) {
		return new ConflictException(ErrorCode.CONFLICT_DUPLICATE, Map.of("resource", resource));
	}

	public static ConflictException versionMismatch(String resource) {
		return new ConflictException(ErrorCode.CONFLICT_VERSION_MISMATCH, Map.of("resource", resource));
	}

	public static ConflictException idempotencyConflict(String key) {
		return new ConflictException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT, Map.of("key", key));
	}

	@Override
	public Object[] getMessageArgs() {
		if (getContext().containsKey("resource")) {
			return new Object[]{getContext().get("resource")};
		}
		if (getContext().containsKey("key")) {
			return new Object[]{getContext().get("key")};
		}
		return new Object[0];
	}
}
