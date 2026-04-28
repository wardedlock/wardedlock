package dev.wardedlock.archtest;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

/**
 * Wardedlock architecture gate.
 * <p>
 * Each {@code @ArchTest} field corresponds to a rule documented in
 * "Tiêu chuẩn ArchUnit — Wardedlock" (version 1.0.1-DRAFT). Rule code format:
 * {@code ARCH-<TIER>-<NNN>}. Field names mirror the rule code with underscores.
 * <p>
 * Total: 19 fields = 10 Tier S + 7 Tier A + 2 Tier B.
 * Test naming rule (ARCH-B-012) lives in {@link TestNamingArchTest}.
 */
@AnalyzeClasses(packages = "dev.wardedlock", importOptions = { ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class })
public class ArchitectureTest {

    private static final String ROLE_SERVICE_PACKAGE = "dev.wardedlock.roleservice..";
    private static final String ACCOUNT_SERVICE_PACKAGE = "dev.wardedlock.accountservice..";
    private static final String NOTIFICATION_SERVICE_PACKAGE = "dev.wardedlock.notificationservice..";
    private static final String APP_MANAGEMENT_SERVICE_PACKAGE = "dev.wardedlock.appmanagementservice..";
    private static final String GATEWAY_SERVICE_PACKAGE = "dev.wardedlock.gateway..";

    private static final String[] BUSINESS_SERVICE_PACKAGES = {
            "dev.wardedlock.auth..",
            ROLE_SERVICE_PACKAGE,
            ACCOUNT_SERVICE_PACKAGE,
            NOTIFICATION_SERVICE_PACKAGE,
            APP_MANAGEMENT_SERVICE_PACKAGE,
            GATEWAY_SERVICE_PACKAGE
    };

    // ============================================================
    // Tier S — Mandatory (build-blocking)
    // ============================================================

    /**
     * ARCH-S-001 — SRS §3.6, FR-CORE-005. Hexagonal layering across all services.
     */
    @ArchTest
    public static final ArchRule ARCH_S_001_layered_architecture = layeredArchitecture()
            .consideringAllDependencies()
            .withOptionalLayers(true)
            .layer("Domain").definedBy("dev.wardedlock..domain..")
            .layer("Application").definedBy("dev.wardedlock..application..")
            .layer("AdapterIn").definedBy("dev.wardedlock..adapter.in..")
            .layer("AdapterOut").definedBy("dev.wardedlock..adapter.out..")
            .layer("Infrastructure").definedBy("dev.wardedlock..infrastructure..")
            .whereLayer("AdapterIn").mayNotBeAccessedByAnyLayer()
            .whereLayer("AdapterOut").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("AdapterIn", "Infrastructure")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "AdapterIn", "AdapterOut");

    /** ARCH-S-001 — SRS §3.6. Domain layer must be framework-free POJO. */
    @ArchTest
    public static final ArchRule ARCH_S_001_domain_is_framework_free = noClasses()
            .that().resideInAPackage("dev.wardedlock..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "com.fasterxml.jackson..",
                    "io.lettuce..",
                    "org.hibernate..")
            .allowEmptyShould(true)
            .because("ARCH-S-001: domain must be POJO; frameworks live in adapters/infrastructure");

    /**
     * ARCH-S-002 — FR-CORE-005. Core is a shared contract; must not depend on
     * business modules.
     */
    @ArchTest
    public static final ArchRule ARCH_S_002_core_has_no_business_dependency = noClasses()
            .that().resideInAPackage("dev.wardedlock.core..")
            .should().dependOnClassesThat().resideInAnyPackage(BUSINESS_SERVICE_PACKAGES)
            .allowEmptyShould(true)
            .because("ARCH-S-002 / FR-CORE-005: core is a shared contract, must not know business logic");

    /** ARCH-S-003 — SRS §3.5. Only Auth Service holds the JWT private key. */
    @ArchTest
    public static final ArchRule ARCH_S_003_jwt_signer_only_in_auth = classes()
            .that().implement("dev.wardedlock.core.security.IJwtSigner")
            .or().haveSimpleNameEndingWith("JwtSigner")
            .or().haveSimpleNameEndingWith("PrivateKeyLoader")
            .or().haveSimpleNameEndingWith("KeyRotator")
            .should().resideInAPackage("dev.wardedlock.auth..")
            .allowEmptyShould(true)
            .because("ARCH-S-003 / SRS §3.5: only Auth Service is allowed to hold the private key");

    /** ARCH-S-003 — SRS §3.5. Non-Auth services must not load the private key. */
    @ArchTest
    public static final ArchRule ARCH_S_003_other_services_must_not_load_private_key = noClasses()
            .that().resideInAnyPackage(
                    ROLE_SERVICE_PACKAGE,
                    ACCOUNT_SERVICE_PACKAGE,
                    NOTIFICATION_SERVICE_PACKAGE,
                    APP_MANAGEMENT_SERVICE_PACKAGE,
                    GATEWAY_SERVICE_PACKAGE)
            .should().dependOnClassesThat().haveFullyQualifiedName("java.security.PrivateKey")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("java.security.interfaces.RSAPrivateKey")
            .allowEmptyShould(true)
            .because("ARCH-S-003: non-Auth services verify with public key via JWKS only");

    /**
     * ARCH-S-004 — FR-NOTI-001. Provider SDKs are confined to adapter.out /
     * infrastructure.
     */
    @ArchTest
    public static final ArchRule ARCH_S_004_provider_sdks_isolated_in_adapter_out = noClasses()
            .that().resideOutsideOfPackages(
                    "dev.wardedlock..adapter.out..",
                    "dev.wardedlock..infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.twilio..",
                    "com.vonage..",
                    "com.sendgrid..",
                    "software.amazon.awssdk.services.ses..",
                    "org.simplejavamail..",
                    "jakarta.mail..",
                    "com.sun.mail..",
                    "org.apache.hc.client5..")
            .allowEmptyShould(true)
            .because("ARCH-S-004 / FR-NOTI-001: provider SDKs only allowed in adapter.out / infrastructure");

    /**
     * ARCH-S-004 — FR-NOTI-001. Notification provider implementations must live in
     * adapter.out.notification.
     */
    @ArchTest
    public static final ArchRule ARCH_S_004_provider_implementations_in_adapter_out = classes()
            .that().implement("dev.wardedlock.core.notification.IEmailProvider")
            .or().implement("dev.wardedlock.core.notification.ISmsProvider")
            .should().resideInAPackage("dev.wardedlock..adapter.out.notification..")
            .allowEmptyShould(true)
            .because("ARCH-S-004 / FR-NOTI-001: provider implementations belong in adapter.out.notification");

    /**
     * ARCH-S-005 — NFR-MAIN-004. No standard stream output (System.out /
     * System.err).
     */
    @ArchTest
    public static final ArchRule ARCH_S_005_no_standard_streams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .allowEmptyShould(true);

    /**
     * ARCH-S-005 — NFR-MAIN-004. Stack traces must go through SLF4J, not
     * Throwable.printStackTrace().
     */
    @ArchTest
    public static final ArchRule ARCH_S_005_no_print_stack_trace = noClasses()
            .should().callMethod(Throwable.class, "printStackTrace")
            .orShould().callMethod(Throwable.class, "printStackTrace", java.io.PrintStream.class)
            .orShould().callMethod(Throwable.class, "printStackTrace", java.io.PrintWriter.class)
            .allowEmptyShould(true)
            .because("ARCH-S-005 / NFR-MAIN-004: stack traces must be logged via SLF4J for structured JSON output");

    /** ARCH-S-005 — NFR-MAIN-004. java.util.logging is forbidden; use SLF4J. */
    @ArchTest
    public static final ArchRule ARCH_S_005_no_jul = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
            .allowEmptyShould(true);

    // ============================================================
    // Tier A — Strongly recommended (build-blocking, exemption via
    // FreezingArchRule)
    // ============================================================

    /**
     * ARCH-A-006 — SRS §3.4. Database-per-service: persistence slices must not
     * depend on each other.
     */
    @ArchTest
    public static final ArchRule ARCH_A_006_persistence_slice_independence = SlicesRuleDefinition.slices()
            .matching("dev.wardedlock.(*).adapter.out.persistence..")
            .should().notDependOnEachOther()
            .as("ARCH-A-006: each service's persistence package is independent")
            .allowEmptyShould(true)
            .because("SRS §3.4: cross-service data access must go through internal APIs");

    /**
     * ARCH-A-007 — SRS §3.6. JPA entities live in adapter.out.persistence (not
     * domain).
     */
    @ArchTest
    public static final ArchRule ARCH_A_007_entities_only_in_persistence = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .or().areAnnotatedWith(jakarta.persistence.Embeddable.class)
            .or().areAnnotatedWith(jakarta.persistence.MappedSuperclass.class)
            .should().resideInAPackage("dev.wardedlock..adapter.out.persistence..")
            .allowEmptyShould(true)
            .because("ARCH-A-007: separating JPA entities from domain entities preserves Hexagonal isolation");

    /**
     * ARCH-A-007 — SRS §3.6. Spring Data repositories live in
     * adapter.out.persistence.
     */
    @ArchTest
    public static final ArchRule ARCH_A_007_repositories_only_in_persistence = classes()
            .that().areAssignableTo(org.springframework.data.repository.Repository.class)
            .should().resideInAPackage("dev.wardedlock..adapter.out.persistence..")
            .allowEmptyShould(true)
            .because("ARCH-A-007: repositories are an outbound adapter concern");

    /** ARCH-A-008 — SRS §5.0.1. Controllers live in adapter.in.rest. */
    @ArchTest
    public static final ArchRule ARCH_A_008_controllers_only_in_adapter_in_rest = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .or().areAnnotatedWith(org.springframework.stereotype.Controller.class)
            .should().resideInAPackage("dev.wardedlock..adapter.in.rest..")
            .allowEmptyShould(true)
            .because("ARCH-A-008 / SRS §5.0.1: HTTP entry points belong in adapter.in.rest");

    /**
     * ARCH-A-008 — SRS §5.0.1. Controller methods must return DTOs, ResponseEntity,
     * common collections, or void.
     */
    @ArchTest
    public static final ArchRule ARCH_A_008_controller_returns_dto_or_response_entity = methods()
            .that().areDeclaredInClassesThat()
            .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .or().areAnnotatedWith(org.springframework.stereotype.Controller.class)
            .and().arePublic()
            .should().haveRawReturnType(
                    DescribedPredicate.describe(
                            "DTO in adapter.in.rest.dto, ResponseEntity, Page/Slice, List/Map/Set/Collection, String, byte[], or void",
                            javaClass -> {
                                String name = javaClass.getName();
                                String pkg = javaClass.getPackageName();
                                return (pkg.startsWith("dev.wardedlock") && pkg.contains(".adapter.in.rest.dto"))
                                        || name.equals("org.springframework.http.ResponseEntity")
                                        || name.equals("org.springframework.data.domain.Page")
                                        || name.equals("org.springframework.data.domain.Slice")
                                        || name.equals("java.util.List")
                                        || name.equals("java.util.Map")
                                        || name.equals("java.util.Set")
                                        || name.equals("java.util.Collection")
                                        || name.equals("java.lang.String")
                                        || name.equals("[B")
                                        || name.equals("void");
                            }))
            .allowEmptyShould(true)
            .because(
                    "ARCH-A-008: controllers must not leak domain entities; return shape is restricted to API-safe types");

    /** ARCH-A-009 — Constructor injection only; no field-level @Autowired. */
    @ArchTest
    public static final ArchRule ARCH_A_009_no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .allowEmptyShould(true);

    /**
     * ARCH-A-009 — No jakarta.inject.Inject on fields; constructor injection only.
     */
    @ArchTest
    public static final ArchRule ARCH_A_009_no_jakarta_inject_field = fields()
            .should().notBeAnnotatedWith(jakarta.inject.Inject.class)
            .allowEmptyShould(true)
            .because("ARCH-A-009: constructor injection keeps classes testable without Spring context");

    // ============================================================
    // Tier B — Optional (CI warning, no merge block)
    // ============================================================

    /**
     * ARCH-B-010 — FR-NOTI-004. Domain events must be Java records ending with
     * "Event".
     */
    @ArchTest
    public static final ArchRule ARCH_B_010_domain_events_are_records = classes()
            .that().resideInAPackage("dev.wardedlock..domain.event..")
            .should().beRecords()
            .andShould().haveSimpleNameEndingWith("Event")
            .allowEmptyShould(true)
            .because("ARCH-B-010 / FR-NOTI-004: events must be immutable for stable idempotency keys");

    /**
     * ARCH-B-011 — NFR-SEC-001. java.util.Random has insufficient entropy for
     * security primitives.
     */
    @ArchTest
    public static final ArchRule ARCH_B_011_no_insecure_random = noClasses()
            .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Random")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("org.apache.commons.lang3.RandomUtils")
            .allowEmptyShould(true)
            .because("ARCH-B-011 / NFR-SEC-001: tokens, OTPs, and nonces must be derived from SecureRandom");
}
