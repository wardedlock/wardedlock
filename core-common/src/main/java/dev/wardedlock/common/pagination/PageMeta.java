package dev.wardedlock.common.pagination;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Metadata for a paginated API response.
 *
 * @param nextCursor An encoded token to fetch the next page, or null if there are no more pages.
 * @param hasMore Indicates whether a subsequent page of data exists.
 * @param limit The actual number of items returned in the current page.
 */
public record PageMeta(
        String nextCursor,
        @NotNull Boolean hasMore,
        @Min(0) int limit
) {
}
