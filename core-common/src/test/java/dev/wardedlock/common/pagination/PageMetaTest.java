package dev.wardedlock.common.pagination;

import org.junit.jupiter.api.Test;

import static dev.wardedlock.common.testing.ValidationTestSupport.assertInvalidOn;
import static dev.wardedlock.common.testing.ValidationTestSupport.assertValid;

class PageMetaTest {

    @Test
    void shouldPassValidationForValidMeta() {
        assertValid(new PageMeta("token", true, 50));
    }

    @Test
    void shouldPassValidationWhenNextCursorIsNull() {
        assertValid(new PageMeta(null, false, 0));
    }

    @Test
    void shouldFailValidationWhenHasMoreIsNull() {
        assertInvalidOn(new PageMeta(null, null, 10), "hasMore");
    }

    @Test
    void shouldFailValidationWhenLimitIsNegative() {
        assertInvalidOn(new PageMeta(null, false, -1), "limit");
    }
}