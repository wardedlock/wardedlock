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
- Format: `wardedlock:{namespace}:{purpose}:{id}`

---

## 📢 Event Names
Located in `dev.wardedlock.common.keys.EventNames`.

Centralized event type names for the platform's Event-Driven Architecture. Use these when publishing or subscribing to messages on the event bus (e.g., Kafka, RabbitMQ).

**Example Namespaces:**
- `identity.*`: User and Auth events.
- `audit.*`: System audit logs.
- `security.*`: Threat detection and alert events.
