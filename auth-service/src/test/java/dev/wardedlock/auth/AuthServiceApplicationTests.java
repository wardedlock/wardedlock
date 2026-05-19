package dev.wardedlock.auth;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import dev.wardedlock.common.testing.infrastructure.redis.RedisTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001, FR-TEST-003, NFR-TEST-001, NFR-TEST-002. Auth service smoke integration test.
 * Verifies that the Spring Boot application context loads successfully when integrated
 * with dynamic PostgreSQL (alpine 18) and Redis (alpine 7) test containers.
 */
@WardedlockIntegrationTest(infrastructure = {
    PostgresTestInfrastructure.class,
    RedisTestInfrastructure.class
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
