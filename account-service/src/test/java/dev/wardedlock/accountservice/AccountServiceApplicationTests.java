package dev.wardedlock.accountservice;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001, FR-TEST-003, NFR-TEST-001. Account service smoke integration test.
 * Verifies that the Spring Boot application context loads successfully when integrated
 * with a dynamically orchestrated, isolated PostgreSQL test container running alpine.
 */
@WardedlockIntegrationTest(infrastructure = { PostgresTestInfrastructure.class })
class AccountServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
