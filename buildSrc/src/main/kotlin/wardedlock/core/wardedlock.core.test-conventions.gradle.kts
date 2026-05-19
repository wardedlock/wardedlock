import wardedlock.testing.FlywayDatabaseMap

/**
 * Gradle Convention Plugin for centralizing and standardizing test behavior across WardedLock monorepo.
 * Aligns with SRS §5.8 (FR-TEST-002, FR-TEST-006, FR-TEST-007, NFR-TEST-004, ARCH-B-016).
 *
 * This plugin:
 * 1. Automatically activates the 'test' Spring profile for JVM tests.
 * 2. Maps microservices dynamically to their respective databases and injects Flyway paths.
 * 3. Supports quick-loop execution by skipping infrastructure tests if requested via
 *    '-PskipInfrastructureTests' or System property '-Dwardedlock.test.skipInfrastructure=true'.
 */
plugins {
    java
}

tasks.withType<Test> {
    // Configure JUnit 5 Platform
    useJUnitPlatform {
        // Skip tags for dynamic database containers/infrastructures if quick-skip flag is present
        if (project.hasProperty("skipInfrastructureTests") || System.getProperty("wardedlock.test.skipInfrastructure") == "true") {
            excludeTags("infrastructure")
        }
    }

    // 1. Force the active profile to be 'test'
    systemProperty("spring.profiles.active", "test")

    // 2. Inject Flyway migration root path and specific database target name dynamically
    val dbName = FlywayDatabaseMap.getDatabaseForProject(project.name)
    if (dbName != null) {
        systemProperty("wardedlock.flyway.migrations-root", rootProject.file("infra/migrations").absolutePath)
        systemProperty("wardedlock.flyway.database", dbName)
    }
}
