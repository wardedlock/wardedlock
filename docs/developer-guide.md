# WardedLock Developer Guide: Adding a New Microservice

A step-by-step playbook to bootstrap, configure, test, and run a new microservice module within the WardedLock monorepo.

---

## 1. How It Works

Project-specific patterns you must understand before adding a service.

### 1.1 Opt-In Convention Plugins

`core-common` is a thin shared library — utilities, base classes, shared exceptions, testing scaffolding. It does **not** pull any Spring Boot starters onto your service's classpath.

Each capability (Web MVC, JPA, Redis, Mail, …) is packaged as a convention plugin under `buildSrc/`. A service explicitly applies only the plugins it needs. If a plugin is not applied, those classes do not exist on the classpath — compile-time guarantee.

Available plugins:

| Category    | Plugin ID                          | What it brings                                            |
|-------------|------------------------------------|-----------------------------------------------------------|
| Core        | `wardedlock.core.java-conventions` | Java 25 toolchain, UTF-8 encoding, test configuration     |
| Core        | `wardedlock.core.boot`             | Spring Boot plugin, dependency management, `bootJar` task |
| Core        | `wardedlock.core.test-conventions` | JUnit 5 platform, test task defaults                      |
| Core        | `wardedlock.core.i18n-conventions` | Native-to-ASCII property file checking                    |
| Web         | `wardedlock.web.mvc`               | `spring-boot-starter-web` + `starter-validation`          |
| Web         | `wardedlock.web.webflux`           | `spring-boot-starter-webflux` + `starter-validation`      |
| Web         | `wardedlock.web.security`          | Spring Security OAuth2 resource server                    |
| Data        | `wardedlock.data.jpa`              | Spring Data JPA, Flyway client, PostgreSQL driver, Testcontainers PostgreSQL |
| Data        | `wardedlock.data.redis`            | `spring-boot-starter-data-redis` (blocking)               |
| Data        | `wardedlock.data.redis-reactive`   | `spring-boot-starter-data-redis-reactive`                 |
| Integration | `wardedlock.integration.mail`      | `spring-boot-starter-mail`                                |
| Codegen     | `wardedlock.codegen.mapstruct`     | MapStruct annotation processor                            |

### 1.2 Test Classpath Is Symmetrical

Capability plugins also inject the matching test-scope dependencies. For example, `wardedlock.data.jpa` adds `org.testcontainers:postgresql` to `testImplementation` automatically. You do not need to declare test infra libraries yourself — the plugin handles it.

The integration test annotation `@WardedlockIntegrationTest(infrastructure = { ... })` spins up only the Testcontainers you explicitly list. Declaring nothing means no container is started.

### 1.3 Database-per-Service via FlywayDatabaseMap

Services that persist state own one logical PostgreSQL database. The mapping `subproject → database` lives in `FlywayDatabaseMap` as the single source of truth. Cross-service joins are strictly forbidden.

### 1.4 Externalized Flyway

Migrations live under `infra/migrations/<database>/` and are applied by a dedicated runner job, not at service boot. Every service sets `spring.flyway.enabled: false`.

### 1.5 Strict Placeholder Format

All infrastructure config keys must use `${VAR:?<error message>}`. Missing env vars crash the service at startup with a readable error instead of silently falling back to empty strings.

---

## 2. Step-by-Step Playbook

The example below uses `billing-service`. Substitute your own name throughout.

### Step 1: Create the Module & Register

Create `<service-name>/` at the monorepo root and register it in [settings.gradle.kts](../settings.gradle.kts):

```kotlin
include("billing-service")
```

### Step 2: Configure `build.gradle.kts`

Apply the core plugins, then opt into the capabilities your service needs. Always declare the dependency on `:core-common`:

```kotlin
plugins {
    id("wardedlock.core.java-conventions")
    id("wardedlock.core.boot")
    id("wardedlock.web.mvc")           // Web MVC + Validation
    id("wardedlock.data.jpa")          // JPA + Flyway + PostgreSQL
    id("wardedlock.web.security")      // OAuth2 resource server
    id("wardedlock.codegen.mapstruct") // MapStruct (if this service uses DTO mapping)
}

description = "billing-service"

dependencies {
    implementation(project(":core-common"))
    
    // Service-specific dependencies only
}
```

> [!TIP]
> Look at existing services for reference:
> - Typical MVC + JPA service: `account-service`, `role-service`
> - Reactive gateway: `gateway` (uses `web.webflux` + `data.redis-reactive` instead)
> - Service with mail + reactive Redis: `notification-service`

### Step 3: Create the Application Entry Class

Create `billing-service/src/main/java/dev/wardedlock/billing/BillingServiceApplication.java`:

```java
package dev.wardedlock.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
```

### Step 4: Register the Database Schema

> [!NOTE]
> Skip this step if your service does not use a database (i.e. you did not apply `wardedlock.data.jpa`).

Add the subproject-to-database mapping in [FlywayDatabaseMap.kt](../buildSrc/src/main/kotlin/wardedlock/testing/FlywayDatabaseMap.kt):

```kotlin
object FlywayDatabaseMap {
    private val map = mapOf(
        // ... existing entries ...
        "billing-service" to "wl_billing",
    )
}
```

### Step 5: Create the Database Migration Directory

Create `infra/migrations/<database>/` with at least one baseline file:

* Directory: `infra/migrations/wl_billing/`
* File: `infra/migrations/wl_billing/V1__baseline.sql`

### Step 6: Declare Environment Variables

Add the service's env vars to root `.env.example` (committed) and your local `.env` (gitignored):

```ini
BILLING_SERVICE_PORT=8086
BILLING_DB=wl_billing
BILLING_USER=wardedlock
BILLING_PASSWORD=<local password>
```

### Step 7: Configure `application.yml`

Create `billing-service/src/main/resources/application.yml`. Use the strict `${VAR:?<error>}` placeholder format for every infrastructure key:

```yaml
spring:
  application:
    name: billing-service
  config:
    import:
      - optional:file:.env
      - optional:file:../.env
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:?POSTGRES_HOST is required}:${POSTGRES_PORT:?POSTGRES_PORT is required}/${BILLING_DB:?BILLING_DB is required}
    username: ${BILLING_USER:?BILLING_USER is required}
    password: ${BILLING_PASSWORD:?BILLING_PASSWORD is required}
  flyway:
    enabled: false

server:
  port: ${BILLING_SERVICE_PORT:?BILLING_SERVICE_PORT is required}
```

> [!NOTE]
> Do not add infrastructure keys to `application-test.yml` — the test framework injects them dynamically.

### Step 8: Write Integration Smoke Tests

Create `billing-service/src/test/java/dev/wardedlock/billing/BillingServiceApplicationTests.java`. Declare only the Testcontainers infrastructure your service uses:

```java
package dev.wardedlock.billing;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import org.junit.jupiter.api.Test;

@WardedlockIntegrationTest(infrastructure = {
    PostgresTestInfrastructure.class
})
class BillingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Available infrastructure classes are documented in the `dev.wardedlock.common.testing.infrastructure` package. Declare any combination — including none.

### Step 9: Configure Development Runner

Register the service in the root [package.json](../package.json):

```json
"billing": "node scripts/dev-run.js :billing-service:bootRun"
```

Add the alias to the concurrent runner in the `"dev:apps-only"` script:

```json
"dev:apps-only": "concurrently ... \"npm:billing\""
```

### Step 10: Verify the New Service

Ensure the service builds, passes checks, and boots up successfully:

1.  **Run Gradle Verification**:
    Run a clean check to compile the service and execute the integration test suite:
    ```bash
    ./gradlew :billing-service:clean :billing-service:check
    ```
2.  **Verify Local Startup**:
    Start the service locally using your registered dev command to verify that all strict environment placeholders (`${VAR:?error}`) validate successfully and the Spring Context boots without issues:
    ```bash
    npm run billing
    ```