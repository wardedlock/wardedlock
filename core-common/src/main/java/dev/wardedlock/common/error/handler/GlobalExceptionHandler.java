package dev.wardedlock.common.error.handler;

import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.ErrorEnvelope;
import dev.wardedlock.common.error.ErrorResponse;
import dev.wardedlock.common.error.exception.AppException;
import dev.wardedlock.common.error.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for all controllers.
 * Maps exceptions to structured ErrorResponse with data-driven logging.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final ErrorEnvelopeFactory envelopeFactory;

	public GlobalExceptionHandler(ErrorEnvelopeFactory envelopeFactory) {
		this.envelopeFactory = envelopeFactory;
	}

	/**
	 * AppException: special-cases Retry-After header for rate limits.
	 */
	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
		log.warn("AppException: code={} message={}", ex.getErrorCode(), ex.getMessage());
		ErrorEnvelope envelope = envelopeFactory.create(ex);
		
		ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.getErrorCode().getStatus());
		
		if (ex instanceof RateLimitException rl && rl.getRetryAfter() != null) {
			builder.header(HttpHeaders.RETRY_AFTER, String.valueOf(rl.getRetryAfter().toSeconds()));
		}
		
		return builder.body(new ErrorResponse(envelope));
	}

	/**
	 * Catch-all: factory dispatches to the right ErrorCode; log level derived from HTTP status.
	 */
	@ExceptionHandler(Throwable.class)
	public ResponseEntity<ErrorResponse> handleAny(Throwable ex) {
		ErrorEnvelope envelope = envelopeFactory.create(ex);
		ErrorCode code = ErrorCode.valueOf(envelope.code());
		
		int statusValue = code.getStatus().value();
		if (statusValue >= 500) {
			log.error("Server error: {}", ex.getClass().getSimpleName(), ex);
		} else {
			log.debug("Client error: {} {}", ex.getClass().getSimpleName(), ex.getMessage());
		}
		
		return ResponseEntity.status(code.getStatus()).body(new ErrorResponse(envelope));
	}
}
