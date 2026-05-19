package dev.wardedlock.common.testing.infrastructure.mail;

import dev.wardedlock.common.testing.infrastructure.TestInfrastructure;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * FR-TEST-003. Mailpit SMTP mock server container orchestration.
 * Spins up an isolated mailpit/mailpit:v1.18 container for reliable mail testing.
 */
public final class MailTestInfrastructure implements TestInfrastructure {

    private static final String IMAGE_NAME = "axllent/mailpit:v1.18";
    private static final int SMTP_PORT = 1025;

    private final GenericContainer<?> container;

    public MailTestInfrastructure() {
        this.container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withExposedPorts(SMTP_PORT);
    }

    @Override
    public void start() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    @Override
    public void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", container::getHost);
        registry.add("spring.mail.port", () -> container.getMappedPort(SMTP_PORT));
    }
}
