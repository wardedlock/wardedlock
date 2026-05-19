package dev.wardedlock.common.testing.infrastructure;

import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * FR-CORE-011, FR-TEST-003. Base contract for starting test containers and
 * registering their dynamic properties. Enables zero-hardcode, dynamic environment-isolated
 * integration testing by orchestrating test container lifecycles and dynamically binding
 * connection properties to the Spring Boot test context.
 */
public interface TestInfrastructure {

    void start();

    void registerProperties(DynamicPropertyRegistry registry);
}
