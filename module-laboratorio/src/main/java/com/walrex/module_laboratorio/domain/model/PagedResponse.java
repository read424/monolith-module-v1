package com.walrex.module_laboratorio.domain.model;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty,
        boolean hasMore
) {
    /**
     * Factory method. Expects page to be 1-indexed (first page = 1).
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 1,
                page >= totalPages,
                content.isEmpty(),
                (long) page * size < totalElements
        );
    }

    public <R> PagedResponse<R> map(Function<T, R> mapper) {
        return new PagedResponse<>(
                content.stream().map(mapper).toList(),
                page, size, totalElements, totalPages, first, last, empty, hasMore
        );
    }
}
