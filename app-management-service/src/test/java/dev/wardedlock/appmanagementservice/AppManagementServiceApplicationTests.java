package dev.wardedlock.appmanagementservice;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001, FR-TEST-003, NFR-TEST-001. App Management service smoke integration test.
 * Verifies that the Spring Boot application context loads successfully when integrated
 * with a dynamically orchestrated, isolated PostgreSQL test container running alpine.
 */
@WardedlockIntegrationTest(infrastructure = { PostgresTestInfrastructure.class })
class AppManagementServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
