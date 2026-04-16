package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;
    private PageMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Integer numberOfElements;
        private Boolean first;
        private Boolean last;
        private Boolean hasPrevious;
        private Boolean hasNext;
    }
}
