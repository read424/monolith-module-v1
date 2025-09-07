package com.walrex.module_partidas.domain.model;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenTachoResponse {
    /**
     * Lista de almacenes tacho de la página actual
     */
    private List<AlmacenTacho> almacenes;

    /**
     * Total de registros en la consulta (sin paginación)
     */
    private Integer totalRecords;

    /**
     * Número total de páginas disponibles
     */
    private Integer totalPages;

    /**
     * Página actual (base 0)
     */
    private Integer currentPage;

    /**
     * Tamaño de página solicitado
     */
    private Integer pageSize;

    /**
     * Indica si existe una página siguiente
     */
    private Boolean hasNext;

    /**
     * Indica si existe una página anterior
     */
    private Boolean hasPrevious;

    /**
     * Número de registros en la página actual
     */
    public Integer getCurrentPageSize() {
        return almacenes != null ? almacenes.size() : 0;
    }

    /**
     * Indica si es la primera página
     */
    public Boolean isFirstPage() {
        return currentPage == 0;
    }

    /**
     * Indica si es la última página
     */
    public Boolean isLastPage() {
        return !hasNext;
    }
}
