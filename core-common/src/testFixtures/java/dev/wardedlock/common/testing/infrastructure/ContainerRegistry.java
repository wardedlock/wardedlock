package dev.wardedlock.common.testing.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FR-TEST-003. Centralized thread-safe registry for lazy initialization and lifecycle management
 * of shared Testcontainers infrastructures. Guarantees a singleton container instance per type,
 * optimizing testing execution speed and resources.
 */
public final class ContainerRegistry {

    private static final Map<Class<? extends TestInfrastructure>, TestInfrastructure> REGISTRY = new ConcurrentHashMap<>();

    private ContainerRegistry() {}

    /**
     * Resolves, lazily starts, and registers a singleton {@link TestInfrastructure} container instance.
     *
     * @param type the concrete class of the required TestInfrastructure
     * @param <T> the type of TestInfrastructure
     * @return the started TestInfrastructure instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends TestInfrastructure> T get(Class<T> type) {
        return (T) REGISTRY.computeIfAbsent(type, key -> {
            try {
                T infra = type.getDeclaredConstructor().newInstance();
                infra.start();
                return infra;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize test infrastructure: " + type.getName(), e);
            }
        });
    }
}
