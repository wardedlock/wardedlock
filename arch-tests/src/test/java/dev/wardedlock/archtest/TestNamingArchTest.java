package dev.wardedlock.archtest;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Test naming convention gate (ARCH-B-012).
 * <p>
 * This file is separate from {@link ArchitectureTest} because it scans test
 * code
 * (via {@link ImportOption.OnlyIncludeTests}), while ArchitectureTest scans
 * production code.
 * <p>
 * Allowed naming suffixes:
 * <ul>
 * <li>{@code *Test} — unit tests, run via Gradle {@code test} task.</li>
 * <li>{@code *IT} — integration tests, run via Gradle {@code integrationTest}
 * task.</li>
 * <li>{@code *E2E} — end-to-end tests, run via Gradle {@code e2eTest}
 * task.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "dev.wardedlock", importOptions = ImportOption.OnlyIncludeTests.class)
public class TestNamingArchTest {

    /** Predicate matching JavaMethods annotated with JUnit 5 {@code @Test}. */
    private static final DescribedPredicate<JavaMethod> ANNOTATED_WITH_JUNIT_TEST = new DescribedPredicate<JavaMethod>(
            "annotated with @org.junit.jupiter.api.Test") {
        @Override
        public boolean test(JavaMethod method) {
            return method.isAnnotatedWith(org.junit.jupiter.api.Test.class);
        }
    };

    /**
     * ARCH-B-012 — NFR-MAIN-001. Test class naming feeds Gradle source-set routing.
     */
    @ArchTest
    public static final ArchRule ARCH_B_012_test_class_naming = classes()
            .that().containAnyMethodsThat(ANNOTATED_WITH_JUNIT_TEST)
            .should().haveSimpleNameEndingWith("Test")
            .orShould().haveSimpleNameEndingWith("Tests")
            .orShould().haveSimpleNameEndingWith("IT")
            .orShould().haveSimpleNameEndingWith("E2E")
            .allowEmptyShould(true)
            .because(
                    "ARCH-B-012 / NFR-MAIN-001: Gradle test/integrationTest/e2eTest tasks select by class-name suffix");
}
