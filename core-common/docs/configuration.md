# ⚙️ Configuration Properties

`core-common` provides type-safe configuration classes that are automatically validated at startup. These properties are prefixed with `wardedlock.*`.

## 🛠️ Available Properties

### 1. Platform Properties (`wardedlock.platform`)
Global settings used by the platform.
- `docs.baseUrl`: The base URL for the documentation site (used to generate error links).

### 2. Security Properties (`wardedlock.security`)
Settings for authentication and authorization.
- `jwt.issuer`: The expected issuer of JWT tokens.
- `jwt.audience`: The expected audience.
- `cors.*`: Standard CORS configuration (allowed origins, methods, etc.).

### 3. Cache Properties (`wardedlock.cache`)
Standardized cache settings for Redis.
- `prefix`: The global prefix for all cache keys.
- `defaultTtl`: Default expiration time for cached items.

### 4. Rate Limiting (`wardedlock.rate-limit`)
API protection thresholds.
- `enabled`: Global toggle.
- `requestsPerSecond`: Default throughput limit.

## 📝 Usage in `application.yml`

```yaml
wardedlock:
  platform:
    docs:
      baseUrl: https://docs.wardedlock.dev
  security:
    jwt:
      issuer: https://auth.wardedlock.dev
  cache:
    prefix: account-service
    defaultTtl: 3600s
```

## 🧪 Validation
All properties are annotated with `@Validated` and Jakarta Validation constraints. If a required property (like `docs.baseUrl`) is missing or malformed, the application will fail to start with a clear error message.
