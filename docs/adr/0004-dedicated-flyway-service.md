# 4. Dedicated Flyway Service for Database Migrations

Date: 2026-04-29

## Status

Accepted

## Context

We are migrating our database initialization strategy for our multi-service, multi-database architecture. Our application currently uses six distinct Postgres databases: `wl_auth`, `wl_account`, `wl_role`, `wl_app_management`, `wl_notification`, and one shared infra database `wl_audit`. 

Two main approaches were considered for executing migrations:
1. **Option A (Spring Boot auto-migrate):** Letting each Spring Boot service run its own migrations on startup (`spring.flyway.enabled=true`) using migrations stored in each service's classpath (`src/main/resources/db/migration`).
2. **Option B (Dedicated Flyway Runner):** Centralizing all SQL migrations in `infra/migrations/` and using a dedicated Flyway runner (Docker Compose service) to apply all migrations before the applications start up.

Additionally, we had to decide on the storage mechanism for the shared audit logs: either retain it on PostgreSQL (`wl_audit`) or stream the data directly to a log aggregation service or object storage (e.g., Loki, S3).

## Decision

We chose **Option B (Dedicated Flyway Runner)** and decided to retain **`wl_audit` on PostgreSQL**.

1. **Why Option B (Dedicated Runner)?**
   The shared `wl_audit` database does not have a single service owner (multiple services write audit logs). If we used Option A, we would either have to create a dummy service just to run `wl_audit`'s migrations, or incorrectly couple the audit schema to one specific service. By centralizing migrations under a dedicated Flyway runner in our Docker Compose stack, we avoid cross-service coupling and keep the migration orchestration in the infrastructure layer where it belongs.

2. **Why PostgreSQL for `wl_audit`?**
   Audit logs require high query-ability (e.g., SQL joins across tenants and user IDs) and ACID guarantees for compliance use cases. At our current scale, PostgreSQL handles this volume comfortably. We will revisit this decision if event volume grows past 1M/day, at which point streaming to Loki or S3 may be necessary.

## Consequences

* **Positive:** Complete decoupling of schema management from application startup. Applications start faster and do not require elevated DDL permissions during normal runtime.
* **Positive:** Shared infrastructure databases like `wl_audit` are migrated cleanly without awkward service dependencies.
* **Negative:** Local development requires developers to explicitly run the Flyway service (e.g., via `npm run migrate`) when they need to sync schemas, though we have wrapped this in our DX scripts (`npm run db:reset`).

## Dev vs Prod execution model

In dev mode, Spring Boot services run natively (foreman/concurrently) while infrastructure (Postgres, Redis, Mailpit, MinIO, Prometheus, Flyway) runs in docker-compose. Therefore `depends_on` does NOT guard the service-to-Flyway ordering. Compensating controls:

1. Each Spring Boot service has `spring.flyway.enabled: false` in application.yml, preventing Spring Boot's classpath-Flyway autoconfig from running on startup. The dedicated Flyway container is the sole migration runner.

2. The `npm run dev` script enforces order: `db:up → migrate → app start`. Developers should never run `foreman start` or `gradle bootRun` directly without first ensuring `npm run migrate` has completed.

In production (future docker-compose.prod.yml), all services will run inside Docker; `depends_on: flyway: { condition: service_completed_successfully }` will be added to each app service, making compensating control (1) redundant but harmless.
