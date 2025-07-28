package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;

import com.walrex.module_almacen.domain.model.OrdenSalidaDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;

/**
 * Mapper para convertir OrdenSalidaDevolucionDTO a
 * OrdenSalidaDevolucionResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenSalidaDevolucionResponseMapper {

    /**
     * Convierte un OrdenSalidaDevolucionDTO a OrdenSalidaDevolucionResponse.
     * 
     * @param dto el DTO de dominio
     * @return el response para la API
     */
    OrdenSalidaDevolucionResponse toResponse(OrdenSalidaDevolucionDTO dto);

    /**
     * Formatea el código del comprobante después del mapeo.
     * Concatena: numeroSerie + '-' + numeroComprobante (rellenado con ceros hasta 8
     * caracteres)
     * 
     * @param dto      el DTO de origen
     * @param response el response de destino
     */
    @AfterMapping
    default void formatearCodigoComprobante(OrdenSalidaDevolucionDTO dto,
            @MappingTarget OrdenSalidaDevolucionResponse response) {
        if (dto.getNumeroComprobante() != null && !dto.getNumeroComprobante().trim().isEmpty()) {
            String numeroSerie = dto.getNumeroSerie() != null ? dto.getNumeroSerie() : "";
            String numeroComprobante = dto.getNumeroComprobante().trim();

            // Rellenar con ceros hasta 8 caracteres
            String numeroComprobanteFormateado = String.format("%08d",
                    Integer.parseInt(numeroComprobante));

            // Concatenar: numeroSerie + '-' + numeroComprobante
            String codigoComprobante = numeroSerie + "-" + numeroComprobanteFormateado;

            response.setCodigoComprobante(codigoComprobante);
        }
    }
}