package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown when a resource is not found.
 */
public final class NotFoundException extends AppException {

	private NotFoundException(ErrorCode errorCode, Map<String, Object> context) {
		super(errorCode, context, Collections.emptyList(), null, null);
	}

	public static NotFoundException resourceNotFound(String resource, String id) {
		return new NotFoundException(ErrorCode.NOT_FOUND, Map.of("resource", resource, "id", id));
	}

	@Override
	public Object[] getMessageArgs() {
		return new Object[]{getContext().get("resource"), getContext().get("id")};
	}
}
