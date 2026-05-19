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
- `jwt.accessTokenTtl`: Access token time-to-live (e.g., `15m`).
- `jwt.refreshTokenTtl`: Refresh token time-to-live (e.g., `30d`).

### 3. Cache Properties (`wardedlock.cache`)
Standardized cache settings for Redis.
- `ttl.session`: Session cache TTL.
- `ttl.refreshToken`: Refresh token cache TTL.
- `ttl.pkce`: PKCE code cache TTL.
- `ttl.loginAttempts`: Login attempts cache TTL.
- `ttl.userPermissions`: User permissions cache TTL.
- `ttl.idempotencyKey`: Idempotency key cache TTL.
- `ttl.jwksCache`: JWKS cache TTL.
- `lettuce.commandTimeout`: Lettuce command timeout.
- `lettuce.poolMaxActive`: Maximum active connections.
- `lettuce.poolMaxIdle`: Maximum idle connections.
- `lettuce.shutdownTimeout`: Shutdown timeout.

### 4. Rate Limiting (`wardedlock.ratelimit`)
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
    ttl:
      session: 30m
      refreshToken: 30d
      pkce: 10m
      loginAttempts: 15m
      userPermissions: 1h
      idempotencyKey: 24h
      jwksCache: 1h
    lettuce:
      commandTimeout: 5s
      poolMaxActive: 100
      poolMaxIdle: 10
      shutdownTimeout: 10s
```

## 🧪 Validation
All properties are annotated with `@Validated` and Jakarta Validation constraints. If a required property (like `docs.baseUrl`) is missing or malformed, the application will fail to start with a clear error message.
