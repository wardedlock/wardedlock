# WardedLock Integration Testing Guide

This guide describes integration testing procedures, commands, and active architectural constraints in the WardedLock monorepo.

---

## 1. Writing Integration Tests

Annotate your smoke or integration test class with `@WardedlockIntegrationTest` and specify the required drivers in the `infrastructure` property.

Available infrastructure classes:
- `PostgresTestInfrastructure` — Spins up a dynamic PostgreSQL container with Flyway migrations.
- `RedisTestInfrastructure` — Spins up a dynamic Redis container.
- `MailTestInfrastructure` — Spins up a Mailpit SMTP mock server container.

```java
package dev.wardedlock.auth;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import dev.wardedlock.common.testing.infrastructure.redis.RedisTestInfrastructure;
import dev.wardedlock.common.testing.infrastructure.mail.MailTestInfrastructure;
import org.junit.jupiter.api.Test;

@WardedlockIntegrationTest(infrastructure = {
    PostgresTestInfrastructure.class,
    RedisTestInfrastructure.class,
    MailTestInfrastructure.class
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {}
}
```

---

## 2. Test Execution Commands

- **Full Suite**: `./gradlew test` (Requires active Docker daemon)
- **Skip Infrastructure (Unit Tests Only)**: `./gradlew test -PskipInfrastructureTests`

---

## 3. Database Constraints & Schema Isolation

- **Isolated Schemas**: `PostgresTestInfrastructure` automatically runs schemas based on service name (e.g. `wl_account`, `wl_auth`).
- **Flyway Migrations**: Applied automatically at context boot from `infra/migrations/`.
- **State Cleanliness**: Always use `@Transactional` or manual teardowns if your tests write to the database.

---

## 4. Architectural Guardrails (ArchUnit Rules)

The build will fail if any of the following boundaries are violated:

- **No Hardcoded Datasource URL**: No `application-test.yml` under `**/src/test/resources/` may contain a `spring.datasource` key. Use dynamic annotation registry mapping instead.
- **Mandatory Integration Annotations**: Every `*ApplicationTests` class inside database-connected services must carry `@WardedlockIntegrationTest`.
- **Literal Path Restrictions**: No microservice test classes may contain string literals pointing to migration directories (`infra/migrations` or `filesystem:../`). Confinement to `core-common` is mandatory.
- **Infrastructure Tag Compliance**: Every Testcontainers integration test must carry the `infrastructure` tag (inherited automatically via `@WardedlockIntegrationTest`).
