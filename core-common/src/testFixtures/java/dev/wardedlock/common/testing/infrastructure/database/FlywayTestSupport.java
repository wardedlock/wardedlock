package dev.wardedlock.common.testing.infrastructure.database;

import org.flywaydb.core.Flyway;

/**
 * FR-TEST-004, NFR-TEST-003, ADR-0004. Programmatic Flyway migration execution
 * helper for Testcontainers databases. Automatically resolves physical migration files from the
 * shared 'infra/migrations/' directory based on Gradle-injected parameters, ensuring that test
 * database schemas evolve identically to production without dual Flyway runner overhead.
 */
public final class FlywayTestSupport {

    private FlywayTestSupport() {}

    public static void migrate(String jdbcUrl, String username, String password) {
        String migrationsRoot = System.getProperty("wardedlock.flyway.migrations-root");
        String database = System.getProperty("wardedlock.flyway.database");

        if (migrationsRoot == null || database == null) {
            return;
        }

        String location = "filesystem:" + migrationsRoot + "/" + database;

        Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations(location)
                .load()
                .migrate();
    }
}
