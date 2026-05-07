package dev.wardedlock.common.pagination;

import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorTest {

    @Test
    void shouldEncodeValueAsUrlSafeBase64WithoutPadding() {
        Cursor c = Cursor.of(Map.of("created_at", "2026-04-29T10:30:00Z", "id", "abc"));

        assertThat(c.value())
                .doesNotContain("+", "/", "=")
                .isNotBlank();
    }

    @Test
    void shouldInjectSchemaVersionInPayload() {
        Cursor c = Cursor.of(Map.of("id", "abc"));

        assertThat(c.payload()).containsEntry("v", 1);
    }

    @Test
    void shouldThrowIllegalArgumentWhenStateIsNull() {
        assertThatThrownBy(() -> Cursor.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowIllegalArgumentWhenStateIsEmpty() {
        assertThatThrownBy(() -> Cursor.of(Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldPreservePayloadEntriesOnRoundTrip() {
        Map<String, Object> state = Map.of(
                "created_at", "2026-04-29T10:30:00Z",
                "id", "abc-123");
        Cursor encoded = Cursor.of(state);

        Cursor decoded = Cursor.parse(encoded.value());

        assertThat(decoded.payload())
                .containsAllEntriesOf(state)
                .containsEntry("v", 1);
    }

    @Test
    void shouldThrowFieldRequiredWhenTokenIsNull() {
        assertThatThrownBy(() -> Cursor.parse(null))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FIELD_REQUIRED));
    }

    @Test
    void shouldThrowFieldRequiredWhenTokenIsBlank() {
        assertThatThrownBy(() -> Cursor.parse("   "))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FIELD_REQUIRED));
    }

    @Test
    void shouldThrowFormatInvalidWhenTokenIsMalformedBase64() {
        assertThatThrownBy(() -> Cursor.parse("!!!not-base64!!!"))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FORMAT_INVALID));
    }

    @Test
    void shouldThrowFormatInvalidWhenTokenIsNotJson() {
        // "hello" base64url-encoded: valid base64 but invalid JSON payload
        String notJson = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("hello".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> Cursor.parse(notJson))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FORMAT_INVALID));
    }

    @Test
    void shouldThrowFormatInvalidWhenVersionIsUnsupported() {
        String v2Json = "{\"v\":2,\"id\":\"abc\"}";
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(v2Json.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> Cursor.parse(token))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FORMAT_INVALID));
    }

    @Test
    void shouldThrowFormatInvalidWhenVersionIsMissing() {
        String noVersionJson = "{\"id\":\"abc\"}";
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(noVersionJson.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> Cursor.parse(token))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FORMAT_INVALID));
    }
}