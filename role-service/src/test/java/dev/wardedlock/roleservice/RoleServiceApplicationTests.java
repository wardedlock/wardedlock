package dev.wardedlock.roleservice;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001, FR-TEST-003, NFR-TEST-001. Role service smoke integration test.
 * Verifies that the Spring Boot application context loads successfully when integrated
 * with a dynamically orchestrated, isolated PostgreSQL test container running alpine.
 */
@WardedlockIntegrationTest(infrastructure = { PostgresTestInfrastructure.class })
class RoleServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
