package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;

import java.util.Collections;

/**
 * Exception thrown for authentication failures.
 */
public final class AuthException extends AppException {

	private AuthException(ErrorCode errorCode) {
		super(errorCode, Collections.emptyMap(), Collections.emptyList(), null, null);
	}

	public static AuthException invalidCredentials() {
		return new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS);
	}

	public static AuthException tokenExpired() {
		return new AuthException(ErrorCode.AUTH_TOKEN_EXPIRED);
	}

	public static AuthException tokenInvalid() {
		return new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
	}

	public static AuthException mfaRequired() {
		return new AuthException(ErrorCode.AUTH_MFA_REQUIRED);
	}

	public static AuthException locked() {
		return new AuthException(ErrorCode.AUTH_LOCKED);
	}
}
