# 🔒 Idempotency Logic

Idempotency ensures that performing the same operation multiple times has the same effect as performing it once. This is critical for distributed systems where network failures can lead to retries.

## 🔑 The Idempotency Key

Wardedlock identifies unique requests using a composite key:
1.  **`Idempotency-Key`**: A client-supplied token (usually a UUID).
2.  **`X-Client-Id`**: The tenant or application identifier.

Both headers must be present to activate idempotency.

## 🛠️ Usage

### 1. Extracting the Key
Use the `IdempotencyKey` record to safely parse headers from an incoming `HttpServletRequest`.

```java
public void handleRequest(HttpServletRequest request) {
    Optional<IdempotencyKey> key = IdempotencyKey.from(request);
    
    if (key.isPresent()) {
        IdempotencyKey idKey = key.get();
        // 1. Check if idKey.key() exists in Redis for idKey.clientId()
        // 2. If exists, return cached response
        // 3. If not, process request and store result
    } else {
        // Process as a non-idempotent request
    }
}
```

### 📏 Constraints
- **Key Length**: Maximum 255 characters.
- **Client ID**: Required if `Idempotency-Key` is provided.

## ❌ Error Handling
- If `Idempotency-Key` is too long, a `ValidationException` is thrown (400 Bad Request).
- If `Idempotency-Key` is provided but `X-Client-Id` is missing, a `ValidationException` is thrown.
- These are automatically handled by the [Global Exception Handler](./error-handling.md).
