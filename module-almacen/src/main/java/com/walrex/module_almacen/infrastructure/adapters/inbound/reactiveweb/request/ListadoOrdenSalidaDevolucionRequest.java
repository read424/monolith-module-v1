package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import com.walrex.module_almacen.domain.model.PaginationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request para filtrar el listado de órdenes de salida por devolución con soporte de paginación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para el listado de órdenes de salida por devolución con paginación")
public class ListadoOrdenSalidaDevolucionRequest {
    
    @Schema(description = "Nombre del cliente para filtrar", example = "Juan Pérez")
    private String nombreCliente;
    
    @Schema(description = "Fecha de inicio para filtrar por rango de fechas", example = "2024-01-01")
    private LocalDate fechaInicio;
    
    @Schema(description = "Fecha de fin para filtrar por rango de fechas", example = "2024-12-31")
    private LocalDate fechaFin;
    
    @Schema(description = "Código de salida para filtrar", example = "SAL-2024-001")
    private String codigoSalida;
    
    @Schema(description = "Número de guía para filtrar", example = "GU-001")
    private String numeroGuia;
    
    @Schema(description = "Parámetros de paginación")
    @Builder.Default
    private PaginationDTO pagination = PaginationDTO.builder()
            .page(0)
            .size(10)
            .sortBy("id_ordensalida")
            .sortDirection("DESC")
            .build();
} 