package dev.wardedlock.common.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.wardedlock.common.testing.ValidationTestSupport.assertInvalidOn;
import static dev.wardedlock.common.testing.ValidationTestSupport.assertValid;
import static org.assertj.core.api.Assertions.assertThat;

class PaginationTest {

    @Test
    void shouldMarkHasMoreTrueWhenCursorIsProvided() {
        Pagination<String> p = Pagination.of(List.of("a", "b"), "next-token", 50);

        assertThat(p.data()).containsExactly("a", "b");
        assertThat(p.pagination().nextCursor()).isEqualTo("next-token");
        assertThat(p.pagination().hasMore()).isTrue();
        assertThat(p.pagination().limit()).isEqualTo(50);
    }

    @Test
    void shouldMarkHasMoreFalseWhenCursorIsNull() {
        Pagination<String> p = Pagination.of(List.of("a"), null, 50);

        assertThat(p.pagination().hasMore()).isFalse();
    }

    @Test
    void shouldReturnEmptyListAndNoCursorForEmpty() {
        Pagination<String> p = Pagination.empty(20);

        assertThat(p.data()).isEmpty();
        assertThat(p.pagination().nextCursor()).isNull();
        assertThat(p.pagination().hasMore()).isFalse();
        assertThat(p.pagination().limit()).isEqualTo(20);
    }

    @Test
    void shouldReportCascadedViolationPathForInvalidMeta() {
        // null hasMore must surface at path "pagination.hasMore" via @Valid cascade
        PageMeta badMeta = new PageMeta(null, null, 10);
        Pagination<String> p = new Pagination<>(List.of(), badMeta);

        assertInvalidOn(p, "pagination.hasMore");
    }

    @Test
    void shouldPassValidationForValidPagination() {
        assertValid(Pagination.of(List.of("x"), "tok", 10));
    }
}