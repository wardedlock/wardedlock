package dev.wardedlock.common.keys;

public final class HttpHeaders {

    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String X_CLIENT_ID = "X-Client-Id";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String WARDEDLOCK_SIGNATURE = "Wardedlock-Signature";

    public static final String X_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    public static final String X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    public static final String X_RATELIMIT_RESET = "X-RateLimit-Reset";

    public static final String TRACEPARENT = "traceparent";
    public static final String TRACESTATE = "tracestate";

    public static final class Mdc {
        public static final String REQUEST_ID = "request_id";
        public static final String TRACE_ID = "trace_id";

        private Mdc() {}
    }

    private HttpHeaders() {}
}
