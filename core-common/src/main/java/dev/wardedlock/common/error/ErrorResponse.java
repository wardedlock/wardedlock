package dev.wardedlock.common.error;

/**
 * Wrapper for the error envelope to ensure the JSON root is "error".
 *
 * @param error the error envelope
 */
public record ErrorResponse(
	ErrorEnvelope error
) {}
