package dev.wardedlock.common.error.handler;

import dev.wardedlock.common.config.PlatformProperties;
import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.ErrorEnvelope;
import dev.wardedlock.common.error.ErrorField;
import dev.wardedlock.common.error.exception.AppException;
import dev.wardedlock.common.keys.HttpHeaders;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Factory for creating ErrorEnvelope from various exception types.
 * Handles localization, MDC extraction, and mapping of standard exceptions to ErrorCodes.
 */
@Component
public class ErrorEnvelopeFactory {

	private final MessageSource messageSource;
	private final PlatformProperties platformProperties;

	private static final Map<Integer, ErrorCode> STATUS_MAPPING = Map.ofEntries(
		Map.entry(HttpStatus.BAD_REQUEST.value(), ErrorCode.VALIDATION_FAILED),
		Map.entry(HttpStatus.UNAUTHORIZED.value(), ErrorCode.AUTH_INVALID_CREDENTIALS),
		Map.entry(HttpStatus.FORBIDDEN.value(), ErrorCode.FORBIDDEN_INSUFFICIENT_SCOPE),
		Map.entry(HttpStatus.NOT_FOUND.value(), ErrorCode.NOT_FOUND),
		Map.entry(HttpStatus.CONFLICT.value(), ErrorCode.CONFLICT_DUPLICATE),
		Map.entry(HttpStatus.TOO_MANY_REQUESTS.value(), ErrorCode.RATE_LIMIT_EXCEEDED),
		Map.entry(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR),
		Map.entry(HttpStatus.SERVICE_UNAVAILABLE.value(), ErrorCode.UPSTREAM_UNAVAILABLE),
		Map.entry(HttpStatus.GATEWAY_TIMEOUT.value(), ErrorCode.UPSTREAM_TIMEOUT),
		Map.entry(HttpStatus.BAD_GATEWAY.value(), ErrorCode.UPSTREAM_BAD_RESPONSE)
	);

	public ErrorEnvelopeFactory(MessageSource messageSource, PlatformProperties platformProperties) {
		this.messageSource = messageSource;
		this.platformProperties = platformProperties;
	}

	/**
	 * Creates an ErrorEnvelope from a Throwable.
	 *
	 * @param ex the exception to map
	 * @return a structured error envelope
	 */
	public ErrorEnvelope create(Throwable ex) {
		return switch (ex) {
			case AppException appEx -> create(appEx.getErrorCode(), appEx.getFields(), appEx.getMessageArgs());
			case MethodArgumentNotValidException validEx -> create(ErrorCode.VALIDATION_FAILED, fieldsOf(validEx), null);
			case ConstraintViolationException constraintEx -> create(ErrorCode.VALIDATION_FAILED, fieldsOf(constraintEx), null);
			case HttpMessageNotReadableException _, MethodArgumentTypeMismatchException _ -> simple(ErrorCode.VALIDATION_FORMAT_INVALID);
			case MissingServletRequestParameterException _ -> simple(ErrorCode.VALIDATION_FIELD_REQUIRED);
			case AccessDeniedException _ -> simple(ErrorCode.FORBIDDEN_INSUFFICIENT_SCOPE);
			case AuthenticationException _ -> simple(ErrorCode.AUTH_INVALID_CREDENTIALS);
			case ResponseStatusException statusEx -> simple(STATUS_MAPPING.getOrDefault(statusEx.getStatusCode().value(), ErrorCode.INTERNAL_ERROR));
			default -> simple(ErrorCode.INTERNAL_ERROR);
		};
	}

	/**
	 * Creates an ErrorEnvelope for a specific ErrorCode and fields.
	 *
	 * @param code   the error code
	 * @param fields the field-level errors
	 * @return a structured error envelope
	 */
	public ErrorEnvelope create(ErrorCode code, List<ErrorField> fields) {
		return create(code, fields, null);
	}

	private ErrorEnvelope create(ErrorCode code, List<ErrorField> fields, Object[] args) {
		String requestId = MDC.get(HttpHeaders.Mdc.REQUEST_ID);
		String traceId = MDC.get(HttpHeaders.Mdc.TRACE_ID);
		String message = resolveMessage(code, args);
		String docUrl = platformProperties.docs().baseUrl() + "/" + code.getDocSlug();

		return new ErrorEnvelope(code.name(), message, requestId, traceId, docUrl, fields);
	}

	private String resolveMessage(ErrorCode code, Object[] args) {
		try {
			String msg = messageSource.getMessage(code.getMessageKey(), args, LocaleContextHolder.getLocale());
			return msg != null ? msg : code.name();
		} catch (Exception e) {
			return code.name();
		}
	}

	private ErrorEnvelope simple(ErrorCode code) {
		return create(code, List.of(), null);
	}

	private List<ErrorField> fieldsOf(MethodArgumentNotValidException ex) {
		return ex.getBindingResult().getFieldErrors().stream()
			.map(f -> new ErrorField(f.getField(), f.getCode(), f.getDefaultMessage()))
			.toList();
	}

	private List<ErrorField> fieldsOf(ConstraintViolationException ex) {
		return ex.getConstraintViolations().stream()
			.map(v -> new ErrorField(v.getPropertyPath().toString(),
				v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
				v.getMessage()))
			.toList();
	}
}
