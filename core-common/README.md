# 🛡️ Wardedlock Core Common

The foundation of the Wardedlock platform. `core-common` provides a standardized set of utilities, configurations, and API contracts used across all microservices to ensure consistency, security, and developer velocity.

---

## 🚀 Why Use Core Common?

- **Standardization**: Uniform error responses, pagination formats, and idempotency logic across the entire platform.
- **Dry Architecture**: Avoid reimplementing cross-cutting concerns (like cursor-based pagination or global error handling) in every service.
- **Security by Default**: Pre-configured security properties and header constants.
- **Observability**: Seamless integration with distributed tracing via standard error envelopes.

---

## 🛠️ Key Components

### 1. ⚠️ Error Handling System
The error handling system transforms exceptions into structured, localized JSON responses.

- **`ErrorCode`**: A central registry of all platform errors, mapping to HTTP statuses and documentation.
- **`ErrorEnvelope`**: The standard JSON structure for error responses, including `requestId`, `traceId`, and field-level validation details.
- **Global Handler**: An auto-configured `@ControllerAdvice` that catches custom `AppException` types and standard Spring exceptions.

**How to use:**
```java
// Throwing a structured exception
throw new NotFoundException("User not found with id: " + id);

// Throwing a validation exception with field details
throw ValidationException.fieldRequired("email");
```

### 2. 📑 Pagination Utilities
Implements **Opaque Cursor-Based Pagination**, which is more performant and stable than traditional offset-based pagination for large datasets.

- **`Cursor`**: Encodes internal state (like sort keys and IDs) into a Base64URL string. It's versioned to allow schema evolution without breaking client tokens.
- **`Pagination<T>`**: A generic wrapper for list responses that includes a `PageMeta` object.

**How to use:**
```java
// Creating a cursor from a state map
Cursor cursor = Cursor.of(Map.of("createdAt", lastItem.getTimestamp(), "id", lastItem.getId()));

// Wrapping data in a pagination response
return Pagination.of(items, cursor.value(), limit);
```

### 3. 🔒 Idempotency Support
Ensures that "at-least-once" delivery from clients doesn't result in duplicate processing.

- **`IdempotencyKey`**: A composite key parsed from the `Idempotency-Key` and `X-Client-Id` headers.

**How to use:**
```java
Optional<IdempotencyKey> key = IdempotencyKey.from(request);
key.ifPresent(k -> {
    // Check against Redis/DB using k.clientId() and k.key()
});
```

### 4. ⚙️ Configuration Properties
Type-safe configuration blocks for common platform features.

- `CacheProperties`: TTL and prefix management.
- `SecurityProperties`: JWT and CORS settings.
- `RateLimitProperties`: Thresholds for API protection.

---

## 📖 Detailed Guides

For in-depth technical details on specific subsystems, refer to the following guides:

- [Error Handling & Exceptions](./docs/error-handling.md)
- [Pagination & Cursors](./docs/pagination.md)
- [Idempotency Logic](./docs/idempotency.md)
- [Configuration Properties](./docs/configuration.md)
- [Internationalization (i18n)](./docs/i18n.md)
- [Constants & Keys](./docs/constants.md)

---

## 🛠️ Development Setup

### Adding to your service
In your `build.gradle.kts`:
```kotlin
implementation(project(":core-common"))
```

### Auto-Configuration
`core-common` uses Spring Boot's `@AutoConfiguration`. Most features are enabled automatically once the dependency is added. To customize behavior, override the respective `@ConfigurationProperties` in your `application.yml`.
