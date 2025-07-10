package com.walrex.module_almacen.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para parámetros de paginación.
 * Contiene los parámetros básicos para implementar paginación en consultas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Número de página (basado en 0)
     */
    @Builder.Default
    private Integer page = 0;
    
    /**
     * Tamaño de página
     */
    @Builder.Default
    private Integer size = 10;
    
    /**
     * Campo por el cual ordenar
     */
    @Builder.Default
    private String sortBy = "id_ordensalida";
    
    /**
     * Dirección del ordenamiento (ASC o DESC)
     */
    @Builder.Default
    private String sortDirection = "DESC";
    
    /**
     * Calcula el offset para la query SQL
     * @return offset basado en página y tamaño
     */
    public Integer getOffset() {
        return page * size;
    }
    
    /**
     * Valida que los parámetros de paginación sean válidos
     * @return true si son válidos, false en caso contrario
     */
    public boolean isValid() {
        return page >= 0 && size > 0 && size <= 100;
    }
} 