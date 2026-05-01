package dev.wardedlock.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Standard error envelope for all API error responses.
 *
 * @param code      the ErrorCode name (e.g., "VALIDATION_FAILED")
 * @param message   the localized human-readable message
 * @param requestId the request tracking ID from MDC
 * @param traceId   the distributed trace ID from MDC
 * @param docUrl    the URL to the documentation for this error
 * @param fields    list of field-level errors (primarily for validation failures)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorEnvelope(
	String code,
	String message,
	String requestId,
	String traceId,
	String docUrl,
	List<ErrorField> fields
) {}
