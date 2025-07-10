package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response wrapper genérico para datos paginados.
 * Contiene los datos y metadata de paginación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response paginado con metadata de paginación")
public class PaginatedResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Lista de elementos de la página actual")
    private List<T> content;
    
    @Schema(description = "Metadata de paginación")
    private PageMetadata pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Metadata de paginación")
    public static class PageMetadata implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "Número de página actual (basado en 0)", example = "0")
        private Integer page;
        
        @Schema(description = "Tamaño de página", example = "10")
        private Integer size;
        
        @Schema(description = "Total de elementos", example = "150")
        private Long totalElements;
        
        @Schema(description = "Total de páginas", example = "15")
        private Integer totalPages;
        
        @Schema(description = "Número de elementos en la página actual", example = "10")
        private Integer numberOfElements;
        
        @Schema(description = "Indica si es la primera página", example = "true")
        private Boolean first;
        
        @Schema(description = "Indica si es la última página", example = "false")
        private Boolean last;
        
        @Schema(description = "Indica si hay página anterior", example = "false")
        private Boolean hasPrevious;
        
        @Schema(description = "Indica si hay página siguiente", example = "true")
        private Boolean hasNext;
        
        @Schema(description = "Campo por el cual se ordenó", example = "id")
        private String sortBy;
        
        @Schema(description = "Dirección del ordenamiento", example = "DESC")
        private String sortDirection;
        
        /**
         * Calcula los metadatos de paginación
         */
        public static PageMetadata calculate(int page, int size, long totalElements, 
                                           int numberOfElements, String sortBy, String sortDirection) {
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            return PageMetadata.builder()
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .numberOfElements(numberOfElements)
                    .first(page == 0)
                    .last(page >= totalPages - 1)
                    .hasPrevious(page > 0)
                    .hasNext(page < totalPages - 1)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
        }
    }
} 