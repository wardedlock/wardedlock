package dev.wardedlock.notificationservice;

import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import dev.wardedlock.common.testing.infrastructure.database.PostgresTestInfrastructure;
import dev.wardedlock.common.testing.infrastructure.mail.MailTestInfrastructure;
import dev.wardedlock.common.testing.infrastructure.redis.RedisTestInfrastructure;
import org.junit.jupiter.api.Test;

/**
 * FR-TEST-001, FR-TEST-003, NFR-TEST-001. Notification service smoke integration test.
 * Verifies that the Spring Boot application context loads successfully when integrated
 * with dynamic PostgreSQL (alpine 18) and Mailpit SMTP test containers.
 */
@WardedlockIntegrationTest(infrastructure = {
    PostgresTestInfrastructure.class,
    RedisTestInfrastructure.class,
    MailTestInfrastructure.class
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
