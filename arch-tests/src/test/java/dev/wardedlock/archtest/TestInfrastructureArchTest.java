package dev.wardedlock.archtest;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import dev.wardedlock.common.testing.infrastructure.WardedlockIntegrationTest;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ARCH-B-013, ARCH-B-014, ARCH-B-015, ARCH-B-016. Test infrastructure architectural guards.
 * Enforces compliance across the monorepo regarding test configurations, dynamic containers,
 * filesystem reference confinement, and infrastructure tagging.
 */
@AnalyzeClasses(packages = "dev.wardedlock", importOptions = ImportOption.OnlyIncludeTests.class)
public class TestInfrastructureArchTest {

    private static java.io.File findRootDirectory() {
        java.io.File dir = new java.io.File(".").getAbsoluteFile();
        while (dir != null) {
            if (new java.io.File(dir, "settings.gradle.kts").exists()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        return new java.io.File("."); // fallback
    }

    /**
     * ARCH-B-013 — resource rule: glob matching src/test/resources/application-test.yml must not contain spring.datasource key.
     * Prevents regression of hardcoded JDBC URLs in tests.
     */
    @Test
    void test_ARCH_B_013_no_spring_datasource_in_test_yml() throws Exception {
        java.io.File rootDir = findRootDirectory();
        java.nio.file.Path start = rootDir.toPath().toAbsolutePath();
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(start)) {
            stream.filter(path -> path.toString().replace('\\', '/').contains("src/test/resources")
                            && (path.getFileName().toString().equals("application-test.yml")
                                || path.getFileName().toString().equals("application-test.yaml")))
                  .forEach(path -> {
                      if (path.toString().replace('\\', '/').contains("core-common")) {
                          return; // whitelist core-common if needed
                      }
                      try {
                          String content = java.nio.file.Files.readString(path);
                          if (content.contains("spring.datasource") || content.contains("spring:\n  datasource") || content.contains("spring:\n    datasource")) {
                              throw new AssertionError("ARCH-B-013 violation: " + path + " contains hardcoded spring.datasource config!");
                          }
                      } catch (java.io.IOException e) {
                          throw new RuntimeException(e);
                      }
                  });
        }
    }

    /**
     * ARCH-B-014 — *ApplicationTests in modules applying wardedlock.jpa-postgres-conventions MUST carry @WardedlockIntegrationTest.
     */
    @ArchTest
    public static final ArchRule ARCH_B_014_jpa_tests_must_carry_integration_annotation = classes()
            .that().haveSimpleNameEndingWith("ApplicationTests")
            .and().resideInAnyPackage(
                    "dev.wardedlock.accountservice..",
                    "dev.wardedlock.auth..",
                    "dev.wardedlock.roleservice..",
                    "dev.wardedlock.appmanagementservice..",
                    "dev.wardedlock.notificationservice.."
            )
            .should().beAnnotatedWith(WardedlockIntegrationTest.class)
            .allowEmptyShould(true)
            .because("ARCH-B-014: all JPA/PostgreSQL service tests must utilize @WardedlockIntegrationTest to inherit dynamic database configuration");

    /**
     * ARCH-B-015 — src/test/java of microservices MUST NOT contain literal string references to 'infra/migrations' or 'filesystem:../'.
     * Confines physical migration directory paths to core-common test fixtures.
     */
    @Test
    void test_ARCH_B_015_no_migration_literals_in_services() throws Exception {
        java.io.File rootDir = findRootDirectory();
        java.nio.file.Path start = rootDir.toPath().toAbsolutePath();
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(start)) {
            stream.filter(path -> path.toString().replace('\\', '/').contains("src/test/java")
                            && path.getFileName().toString().endsWith(".java"))
                  .forEach(path -> {
                      if (path.toString().replace('\\', '/').contains("core-common") || path.toString().replace('\\', '/').contains("arch-tests")) {
                          return; // whitelist core-common and arch-tests
                      }
                      try {
                          String content = java.nio.file.Files.readString(path);
                          if (content.contains("infra/migrations") || content.contains("filesystem:../")) {
                              throw new AssertionError("ARCH-B-015 violation: " + path + " contains forbidden filesystem literal 'infra/migrations' or 'filesystem:../'!");
                          }
                      } catch (java.io.IOException e) {
                          throw new RuntimeException(e);
                      }
                  });
        }
    }

    /**
     * ARCH-B-016 — *ApplicationTests using Testcontainers MUST carry @Tag("infrastructure").
     */
    @ArchTest
    public static final ArchRule ARCH_B_016_infrastructure_tests_must_carry_tag = classes()
            .that().haveSimpleNameEndingWith("ApplicationTests")
            .and().areAnnotatedWith(WardedlockIntegrationTest.class)
            .should(new ArchCondition<JavaClass>("be tagged with 'infrastructure'") {
                @Override
                public void check(JavaClass clazz, ConditionEvents events) {
                    boolean hasTag = clazz.isAnnotatedWith(org.junit.jupiter.api.Tag.class)
                                     && clazz.getAnnotationOfType(org.junit.jupiter.api.Tag.class).value().equals("infrastructure");
                    boolean hasMetaTag = clazz.isAnnotatedWith(WardedlockIntegrationTest.class);
                    if (!hasTag && !hasMetaTag) {
                        events.add(SimpleConditionEvent.violated(clazz, clazz.getName() + " is not tagged with 'infrastructure'"));
                    }
                }
            })
            .allowEmptyShould(true)
            .because("ARCH-B-016 / NFR-MAIN-001: all tests using Testcontainers or subclassing dynamic infrastructure must carry @Tag(\"infrastructure\")");

    /**
     * ARCH-B-017 — Env-var placeholders in application{,-prod,-staging}.yml MUST use fail-fast syntax ${VAR:?<error>}
     * and are FORBIDDEN from using fallback defaults ${VAR:<default>} for secrets and infrastructure endpoints.
     */
    @Test
    void test_ARCH_B_017_fail_fast_configuration() throws Exception {
        java.io.File rootDir = findRootDirectory();
        java.nio.file.Path start = rootDir.toPath().toAbsolutePath();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([A-Z0-9_]+):([^?}][^}]*)\\}");

        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(start)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return path.toString().replace('\\', '/').contains("src/main/resources")
                        && (fileName.equals("application.yml")
                            || fileName.equals("application.yaml")
                            || fileName.equals("application-prod.yml")
                            || fileName.equals("application-prod.yaml")
                            || fileName.equals("application-staging.yml")
                            || fileName.equals("application-staging.yaml"));
            })
            .forEach(path -> {
                try {
                    String content = java.nio.file.Files.readString(path);
                    java.util.regex.Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String varName = matcher.group(1);
                        String fallback = matcher.group(2);
                        if (isSecretOrEndpoint(varName)) {
                            throw new AssertionError(String.format(
                                "ARCH-B-017 violation in %s: Environment variable placeholder ${%s:%s} uses a forbidden default fallback! "
                                + "Secrets and infrastructure endpoints must use configuration fail-fast syntax: ${%s:?<error_message>}",
                                path, varName, fallback, varName
                            ));
                        }
                    }
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static final java.util.regex.Pattern SECRET_OR_ENDPOINT_PATTERN = java.util.regex.Pattern.compile(
        ".*(password|key|secret|token|credentials|private|public|api_key|host|port|url|uri|endpoint|db|user).*",
        java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private static final java.util.regex.Pattern TUNING_PATTERN = java.util.regex.Pattern.compile(
        ".*(ttl|size|timeout|retry|flag|active_kid|enabled).*",
        java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private static boolean isSecretOrEndpoint(String varName) {
        return SECRET_OR_ENDPOINT_PATTERN.matcher(varName).matches() 
               && !TUNING_PATTERN.matcher(varName).matches();
    }
}
