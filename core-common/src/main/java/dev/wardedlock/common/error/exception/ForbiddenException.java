package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown when access is forbidden.
 */
public final class ForbiddenException extends AppException {

	private ForbiddenException(ErrorCode errorCode, Map<String, Object> context) {
		super(errorCode, context, Collections.emptyList(), null, null);
	}

	public static ForbiddenException insufficientScope(String requiredScope) {
		return new ForbiddenException(ErrorCode.FORBIDDEN_INSUFFICIENT_SCOPE, Map.of("requiredScope", requiredScope));
	}

	public static ForbiddenException tenantMismatch() {
		return new ForbiddenException(ErrorCode.FORBIDDEN_TENANT_MISMATCH, Collections.emptyMap());
	}

	@Override
	public Object[] getMessageArgs() {
		return new Object[]{getContext().get("requiredScope")};
	}
}
