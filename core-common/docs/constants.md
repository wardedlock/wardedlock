# 🔑 Constants & Keys

Standardized keys used for communication between services, header names, and cache identifiers.

---

## 🌐 HTTP Headers
Located in `dev.wardedlock.common.keys.HttpHeaders`.

| Constant | Header Name | Description |
| :--- | :--- | :--- |
| `IDEMPOTENCY_KEY` | `Idempotency-Key` | Token for idempotent requests. |
| `X_CLIENT_ID` | `X-Client-Id` | Unique identifier for the calling client/app. |
| `X_REQUEST_ID` | `X-Request-Id` | Unique ID for request correlation. |
| `X_TRACE_ID` | `X-Trace-Id` | Distributed tracing identifier. |

---

## 💾 Redis Keys
Located in `dev.wardedlock.common.keys.RedisKeys`.

Provides a consistent namespace for caching across services.
- Format: `{namespace}:{purpose}:{id}`

**Example Keys:**
- `auth:session:{uuid}` — User session storage.
- `role:cache:{roleId}` — Role permission cache.
- `ratelimit:ip:{ipAddress}` — IP-based rate limiting counters.
- `idempotency:key:{idempotencyKey}` — Idempotent request deduplication.
- `jwks:cache` — JWKS public key cache.

---

## 📢 Event Names
Located in `dev.wardedlock.common.keys.EventNames`.

Centralized event type names for the platform's Event-Driven Architecture. Use these when publishing or subscribing to messages on the event bus (e.g., Kafka, RabbitMQ).

**Event Streams:**
- `stream:notifications.v1` — Notification events.
- `stream:audit.v1` — Audit log events.
- `stream:notifications.dlq` — Notification dead letter queue.

**User Events:**
- `user.created.v1` — User registration.
- `user.updated.v1` — User profile update.
- `user.deleted.v1` — User deletion.
- `user.email_verified.v1` — Email verification.
- `user.locked.v1` — User account locked.

**Auth Events:**
- `auth.login_success.v1` — Successful login.
- `auth.login_failed.v1` — Failed login attempt.
- `auth.password_changed.v1` — Password change.
- `auth.mfa_enabled.v1` — MFA enabled.
- `auth.idempotency_replay.v1` — Idempotent request replay.

**App Events:**
- `app.created.v1` — Application created.
- `app.secret_rotated.v1` — Application secret rotated.

**Webhook Events:**
- `webhook.delivered.v1` — Webhook delivery success.
- `webhook.failed.v1` — Webhook delivery failure.
- `webhook.disabled.v1` — Webhook disabled.
