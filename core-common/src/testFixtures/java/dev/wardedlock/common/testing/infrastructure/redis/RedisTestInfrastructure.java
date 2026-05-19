package dev.wardedlock.common.testing.infrastructure.redis;

import dev.wardedlock.common.testing.infrastructure.TestInfrastructure;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * FR-TEST-003, NFR-TEST-002. Redis test infrastructure container orchestration.
 * Spins up an isolated redis:8-alpine container and registers properties.
 */
public final class RedisTestInfrastructure implements TestInfrastructure {

    private static final String IMAGE_NAME = "redis:8-alpine";
    private static final int PORT = 6379;

    private final GenericContainer<?> container;

    public RedisTestInfrastructure() {
        this.container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withExposedPorts(PORT);
    }

    @Override
    public void start() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    @Override
    public void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", container::getHost);
        registry.add("spring.data.redis.port", () -> container.getMappedPort(PORT));
        registry.add("spring.data.redis.password", () -> "");
    }
}
