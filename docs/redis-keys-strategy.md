# Redis Keys Strategy

This document defines the standard caching and session keys used in Redis across the Wardedlock project.

## Naming Convention
Keys follow a namespaced pattern: `<service/domain>:<entity/type>:<identifier>`

## Key Definitions

1. **Auth Session:** `auth:session:<uuid>`
   - TTL: Corresponds to access/refresh token TTL.
   - Purpose: Stores active user sessions or blacklisted tokens.

2. **Role Cache:** `role:cache:<roleId>`
   - TTL: High (e.g. 24 hours).
   - Purpose: Caches RBAC role permissions to prevent DB hits.

3. **Rate Limiting:** `ratelimit:ip:<ipAddress>`
   - TTL: Window based (e.g., 60 seconds).
   - Purpose: Enforces rate limiting per IP.

4. **Idempotency:** `idempotency:key:<idempotencyKey>`
   - TTL: Moderate (e.g., 24 hours).
   - Purpose: Prevents duplicate execution of non-safe mutations.

5. **JWKS Cache:** `jwks:cache`
   - TTL: Extended (e.g., 7 days).
   - Purpose: Caches the public JWKS.

> **Source of Truth:**
> Ensure that all application code references `dev.wardedlock.common.keys.RedisKeys` to prevent typos and ensure type safety.
