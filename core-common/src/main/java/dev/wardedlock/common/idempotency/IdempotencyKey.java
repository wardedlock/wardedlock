package dev.wardedlock.common.idempotency;

import dev.wardedlock.common.error.exception.ValidationException;
import dev.wardedlock.common.keys.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/**
 * Composite key identifying an idempotent client request.
 *
 * <p>Parsed from {@code Idempotency-Key} (request token, client-supplied) and
 * {@code X-Client-Id} (tenant identifier). Both must be present to activate
 * idempotency; absence of {@code Idempotency-Key} indicates a non-idempotent
 * request and is valid.
 *
 * @param clientId namespace under which the key is tracked
 * @param key      idempotency token, max 255 characters
 */
public record IdempotencyKey(
        @NotBlank String clientId,
        @NotBlank @Size(max = 255) String key) {

    private static final int MAX_KEY_LENGTH = 255;

    /**
     * Extracts an idempotency key from request headers, or empty if none.
     *
     * @throws ValidationException if {@code Idempotency-Key} exceeds
     *                             {@value #MAX_KEY_LENGTH} characters, or
     *                             {@code X-Client-Id} is missing while
     *                             {@code Idempotency-Key} is provided
     */
    public static Optional<IdempotencyKey> from(HttpServletRequest request) {
        String key = request.getHeader(HttpHeaders.IDEMPOTENCY_KEY);
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        if (key.length() > MAX_KEY_LENGTH) {
            throw ValidationException.formatInvalid(
                    HttpHeaders.IDEMPOTENCY_KEY,
                    "max " + MAX_KEY_LENGTH + " characters");
        }
        String clientId = request.getHeader(HttpHeaders.X_CLIENT_ID);
        if (clientId == null || clientId.isBlank()) {
            throw ValidationException.fieldRequired(HttpHeaders.X_CLIENT_ID);
        }
        return Optional.of(new IdempotencyKey(clientId, key));
    }
}