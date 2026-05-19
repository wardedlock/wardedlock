package dev.wardedlock.common.testing.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * FR-TEST-003, FR-TEST-007. Meta-annotation for environment-isolated integration tests
 * that automatically orchestrates dynamic Testcontainers infrastructures. Bundles standard
 * Spring Boot test context loading, JUnit platform tagging, and extension hooks to eliminate
 * boilerplate configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Tag("infrastructure")
@ExtendWith(WardedlockInfrastructureExtension.class)
public @interface WardedlockIntegrationTest {

    /**
     * Specifies the dynamic test infrastructure containers required for this integration test.
     * Default is empty.
     *
     * @return array of required TestInfrastructure types
     */
    Class<? extends TestInfrastructure>[] infrastructure() default {};
}
