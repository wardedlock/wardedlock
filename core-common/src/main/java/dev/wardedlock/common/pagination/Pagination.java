package dev.wardedlock.common.pagination;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * A generic wrapper for paginated API responses containing the data payload and metadata.
 *
 * @param <T> The type of the elements in the data list.
 * @param data The list of items for the current page, which can be empty but never null.
 * @param pagination The pagination metadata for the current response.
 */
public record Pagination<T>(
        @NotNull List<T> data,
        @NotNull @Valid PageMeta pagination
) {

    /**
     * Creates a new Pagination instance with the provided data and cursor information.
     *
     * @param <T> The type of elements in the response.
     * @param data The list of elements to include in the response.
     * @param nextCursor The cursor for the next page, or null if the end of data is reached.
     * @param limit The size of the returned data.
     * @return A fully constructed Pagination object.
     */
    public static <T> Pagination<T> of(List<T> data, String nextCursor, int limit){
        boolean hasMore = nextCursor != null;
        PageMeta meta = new PageMeta(nextCursor, hasMore, limit);

        return new Pagination<>(data, meta);

    }

    /**
     * Creates an empty Pagination instance representing a state with no results.
     *
     * @param <T> The type of elements in the response.
     * @param limit The requested limit size.
     * @return A Pagination object containing an empty list and indicating no further pages.
     */
    public static <T> Pagination<T> empty(int limit){
        return new Pagination<>(List.of(), new PageMeta(null, false, limit));
    }
}
