# ⚠️ Error Handling & Exceptions

Wardedlock uses a unified error handling strategy to provide consistent feedback to API consumers and internal developers.

## 🏗️ Architecture

The system consists of three main layers:
1.  **Domain Exceptions**: Specific exception classes (e.g., `NotFoundException`, `ConflictException`) that carry semantic meaning.
2.  **The Envelope**: A standard JSON structure (`ErrorEnvelope`) that wraps all error details.
3.  **The Handler**: A global exception handler that intercepts exceptions and translates them into `ErrorResponse`.

## 📦 Standard Error Envelope

Every error response follows this schema:

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "The request was invalid.",
    "requestId": "req-123",
    "traceId": "trace-456",
    "docUrl": "https://docs.wardedlock.dev/errors/validation-failed",
    "fields": [
      {
        "path": "email",
        "code": "invalid_format",
        "message": "Must be a valid email address."
      }
    ]
  }
}
```

## 🛠️ Usage for Developers

### 1. Throwing Common Exceptions
Always prefer specific exceptions over generic ones.

| Exception | HTTP Status | Description |
| :--- | :--- | :--- |
| `NotFoundException` | 404 | Resource does not exist. |
| `ConflictException` | 409 | State conflict (e.g., duplicate unique key). |
| `AuthException` | 401 | Authentication failure. |
| `ForbiddenException` | 403 | Insufficient permissions. |
| `ValidationException` | 400 | Client input is invalid. |
| `UpstreamException` | 502/503/504 | Dependency failure. |

### 2. Validation Errors
`ValidationException` provides factory methods for common scenarios:

```java
// Missing field
throw ValidationException.fieldRequired("username");

// Invalid format
throw ValidationException.formatInvalid("age", "must be a positive number");
```

### 3. Internationalization (i18n)
Error messages are localized using Spring's `MessageSource`. Each `ErrorCode` maps to a message key: `error.<code_name>`.

Example: `ErrorCode.NOT_FOUND` maps to `error.not_found`.

## 🔍 Observability
- **`requestId`**: Unique identifier for the specific HTTP request (from MDC).
- **`traceId`**: Distributed trace ID (e.g., from Zipkin/Brave) to correlate logs across services.
- **`docSlug`**: Generated from the error code to link directly to troubleshooting guides.
