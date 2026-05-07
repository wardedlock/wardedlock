package dev.wardedlock.common.idempotency;

import dev.wardedlock.common.error.ErrorCode;
import dev.wardedlock.common.error.exception.ValidationException;
import dev.wardedlock.common.keys.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeyTest {

    @Test
    void shouldReturnEmptyWhenNoHeadersProvided() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        assertThat(IdempotencyKey.from(req)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenOnlyClientIdProvided() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.X_CLIENT_ID, "client-42");

        assertThat(IdempotencyKey.from(req)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenIdempotencyKeyIsBlank() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.IDEMPOTENCY_KEY, "   ");
        req.addHeader(HttpHeaders.X_CLIENT_ID, "client-42");

        assertThat(IdempotencyKey.from(req)).isEmpty();
    }

    @Test
    void shouldReturnKeyWhenBothHeadersProvided() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.IDEMPOTENCY_KEY, "req-token-abc");
        req.addHeader(HttpHeaders.X_CLIENT_ID, "client-42");

        Optional<IdempotencyKey> result = IdempotencyKey.from(req);

        assertThat(result).hasValueSatisfying(k -> {
            assertThat(k.key()).isEqualTo("req-token-abc");
            assertThat(k.clientId()).isEqualTo("client-42");
        });
    }

    @Test
    void shouldThrowFieldRequiredWhenClientIdIsMissing() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.IDEMPOTENCY_KEY, "req-token");

        assertThatThrownBy(() -> IdempotencyKey.from(req))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FIELD_REQUIRED));
    }

    @Test
    void shouldAcceptKeyAtMaxLength() {
        String maxKey = "a".repeat(255);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.IDEMPOTENCY_KEY, maxKey);
        req.addHeader(HttpHeaders.X_CLIENT_ID, "client-42");

        assertThat(IdempotencyKey.from(req)).isPresent();
    }

    @Test
    void shouldThrowFormatInvalidWhenKeyExceedsMaxLength() {
        String tooLong = "a".repeat(256);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.IDEMPOTENCY_KEY, tooLong);
        req.addHeader(HttpHeaders.X_CLIENT_ID, "client-42");

        assertThatThrownBy(() -> IdempotencyKey.from(req))
                .isInstanceOfSatisfying(ValidationException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_FORMAT_INVALID));
    }
}