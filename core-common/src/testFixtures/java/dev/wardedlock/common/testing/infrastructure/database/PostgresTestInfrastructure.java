package dev.wardedlock.common.testing.infrastructure.database;

import dev.wardedlock.common.testing.infrastructure.TestInfrastructure;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * FR-TEST-003, NFR-TEST-001. PostgreSQL test infrastructure container orchestration.
 * Spins up an isolated postgres:18-alpine container and dynamically applies migrations.
 */
public final class PostgresTestInfrastructure implements TestInfrastructure {

    private static final String IMAGE_NAME = "postgres:18-alpine";

    private final PostgreSQLContainer<?> container;

    public PostgresTestInfrastructure() {
        String dbName = System.getProperty("wardedlock.flyway.database");
        this.container = new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withDatabaseName(dbName != null ? dbName : "wl_test")
                .withUsername("postgres")
                .withPassword("postgres");
    }

    @Override
    public void start() {
        if (!container.isRunning()) {
            container.start();
            FlywayTestSupport.migrate(
                    container.getJdbcUrl(),
                    container.getUsername(),
                    container.getPassword()
            );
        }
    }

    @Override
    public void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
