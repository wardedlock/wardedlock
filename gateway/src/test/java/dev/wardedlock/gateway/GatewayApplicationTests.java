package dev.wardedlock.gateway;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.redis.RedisTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001. Gateway service smoke integration test.
 * Verifies that the Spring Boot gateway application context loads successfully
 * with dynamic Redis (Testcontainers) infrastructure components.
 * Gateway is a stateless routing proxy — it has no database, so JPA/Flyway autoconfiguration is excluded.
 */
@WardedlockIntegrationTest(infrastructure = {
    RedisTestInfrastructure.class
})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}

