package dev.wardedlock.common.pagination;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wardedlock.common.error.exception.ValidationException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Opaque, versioned cursor for cursor-based pagination.
 *
 * <p>Encodes a state map as base64url JSON. Schema version is embedded under
 * key {@code "v"} so the format can evolve without breaking outstanding tokens.
 * The cursor is opaque to clients: serialized via {@link JsonValue} as a plain
 * string. Do not put sensitive data in the payload — base64url is encoding,
 * not encryption.
 *
 * <p>v1 payload: {@code {"v": 1, "<sort-key>": "<value>", "id": "<tie-breaker>"}}
 *
 * @param value   base64url token (the only field exposed to clients)
 * @param payload immutable decoded state, including {@code "v"}
 */
public record Cursor(@JsonValue String value, Map<String, Object> payload) {

    private static final int CURRENT_VERSION = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /**
     * Builds a new cursor from a state map. The schema version is added
     * automatically. The map is defensively copied.
     *
     * @throws IllegalArgumentException if {@code state} is null or empty
     * @throws IllegalStateException    if JSON encoding fails (should not occur
     *                                  for primitive payloads)
     */
    public static Cursor of(Map<String, Object> state) {
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Cursor state must not be empty");
        }
        Map<String, Object> withVersion = new HashMap<>(state);
        withVersion.put("v", CURRENT_VERSION);
        try {
            byte[] json = MAPPER.writeValueAsBytes(withVersion);
            String token = ENCODER.encodeToString(json);
            return new Cursor(token, Map.copyOf(withVersion));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode cursor", e);
        }
    }

    /**
     * Parses a client-supplied cursor token. All failure modes map to
     * {@link ValidationException} for structured 400 responses.
     *
     * @throws ValidationException if the token is blank, malformed, or carries an
     *                             unsupported schema version
     */
    @SuppressWarnings("unchecked")
    public static Cursor parse(String token) {
        if (token == null || token.isBlank()) {
            throw ValidationException.fieldRequired("cursor");
        }
        try {
            byte[] json = DECODER.decode(token);
            Map<String, Object> payload = MAPPER.readValue(json, Map.class);
            Object version = payload.get("v");
            if (!(version instanceof Number n) || n.intValue() != CURRENT_VERSION) {
                throw ValidationException.formatInvalid(
                        "cursor", "version " + CURRENT_VERSION);
            }
            return new Cursor(token, Map.copyOf(payload));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw ValidationException.formatInvalid(
                    "cursor", "base64url JSON v" + CURRENT_VERSION);
        }
    }
}