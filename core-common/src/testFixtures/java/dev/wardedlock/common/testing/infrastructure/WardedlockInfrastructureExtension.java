package dev.wardedlock.common.testing.infrastructure;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * FR-TEST-003, FR-TEST-007. Custom JUnit 5 extension that orchestrates the
 * lifecycle
 * of dynamic Testcontainers infrastructures. Intercepts test class execution,
 * resolves
 * infrastructure requirements declared on {@link WardedlockIntegrationTest},
 * and lazily starts
 * required containers through {@link ContainerRegistry} while dynamically
 * injecting connection properties.
 */
public final class WardedlockInfrastructureExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        WardedlockIntegrationTest annotation = testClass.getAnnotation(WardedlockIntegrationTest.class);
        if (annotation == null) {
            return;
        }

        DynamicPropertyRegistry systemRegistry = (name, valueSupplier) -> System.setProperty(name,
                String.valueOf(valueSupplier.get()));

        for (Class<? extends TestInfrastructure> infraClass : annotation.infrastructure()) {
            TestInfrastructure infra = ContainerRegistry.get(infraClass);
            infra.registerProperties(systemRegistry);
        }
    }
}
