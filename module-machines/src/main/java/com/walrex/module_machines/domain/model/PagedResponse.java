package com.walrex.module_machines.domain.model;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResponse<>(data, page, size, totalElements, totalPages,
                page == 1, page >= totalPages);
    }

    public <R> PagedResponse<R> map(Function<T, R> mapper) {
        return new PagedResponse<>(data.stream().map(mapper).toList(),
                page, size, totalElements, totalPages, first, last);
    }
}
