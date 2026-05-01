package dev.wardedlock.common.error;

/**
 * Represents a validation error on a specific field.
 *
 * @param path    the JSON path or field name (e.g., "user.email")
 * @param code    the specific validation code (e.g., "required")
 * @param message the localized error message
 */
public record ErrorField(
	String path,
	String code,
	String message
) {}
