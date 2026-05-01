package dev.wardedlock.common.error.exception;

import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.ErrorField;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown for validation failures.
 */
public final class ValidationException extends AppException {

	private ValidationException(ErrorCode errorCode, List<ErrorField> fields) {
		super(errorCode, Collections.emptyMap(), fields, null, null);
	}

	public static ValidationException failed(List<ErrorField> fields) {
		return new ValidationException(ErrorCode.VALIDATION_FAILED, fields);
	}

	public static ValidationException fieldRequired(String fieldName) {
		ErrorField field = new ErrorField(fieldName, "required", "Field is required");
		return new ValidationException(ErrorCode.VALIDATION_FIELD_REQUIRED, List.of(field));
	}

	public static ValidationException formatInvalid(String fieldName, String expected) {
		ErrorField field = new ErrorField(fieldName, "format", "Invalid format, expected: " + expected);
		return new ValidationException(ErrorCode.VALIDATION_FORMAT_INVALID, List.of(field));
	}
}
