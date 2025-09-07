package com.walrex.module_partidas.domain.model.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenTachoResponseDTO {
    /**
     * Lista de almacenes tacho de la página actual
     */
    private List<PartidaTachoResponse> partidas;

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
}
