package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown for failures in upstream services.
 */
public final class UpstreamException extends AppException {

	private UpstreamException(ErrorCode errorCode, Map<String, Object> context) {
		super(errorCode, context, Collections.emptyList(), null, null);
	}

	public static UpstreamException timeout(String upstream) {
		return new UpstreamException(ErrorCode.UPSTREAM_TIMEOUT, Map.of("upstream", upstream));
	}

	public static UpstreamException unavailable(String upstream) {
		return new UpstreamException(ErrorCode.UPSTREAM_UNAVAILABLE, Map.of("upstream", upstream));
	}

	public static UpstreamException badResponse(String upstream) {
		return new UpstreamException(ErrorCode.UPSTREAM_BAD_RESPONSE, Map.of("upstream", upstream));
	}

	@Override
	public Object[] getMessageArgs() {
		return new Object[]{getContext().get("upstream")};
	}
}
