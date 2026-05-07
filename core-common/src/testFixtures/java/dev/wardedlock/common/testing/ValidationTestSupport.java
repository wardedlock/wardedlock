package dev.wardedlock.common.testing;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test helper for Bean Validation. Reuses a single {@link Validator} instance
 * (validators are thread-safe and expensive to construct).
 */
public final class ValidationTestSupport {

    private static final ValidatorFactory FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private ValidationTestSupport() {}

    /** Returns all constraint violations on {@code object}. */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return VALIDATOR.validate(object);
    }

    /** Asserts {@code object} has no violations. */
    public static <T> void assertValid(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        assertThat(violations)
                .as("expected no violations on %s", object)
                .isEmpty();
    }

    /** Asserts at least one violation exists at {@code propertyPath}. */
    public static <T> void assertInvalidOn(T object, String propertyPath) {
        Set<ConstraintViolation<T>> violations = validate(object);
        assertThat(violations)
                .as("expected violation on path '%s'", propertyPath)
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath));
    }
}