package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walrex.module_almacen.domain.model.OrdenSalidaDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;

/**
 * Mapper para convertir OrdenSalidaDevolucionDTO a
 * OrdenSalidaDevolucionResponse.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenSalidaDevolucionResponseMapper {

    Logger log = LoggerFactory.getLogger(OrdenSalidaDevolucionResponseMapper.class);

    /**
     * Convierte un OrdenSalidaDevolucionDTO a OrdenSalidaDevolucionResponse.
     * 
     * @param dto el DTO de dominio
     * @return el response para la API
     */
    @Mapping(target = "codigoComprobante", source = ".", qualifiedByName = "formatearCodigoComprobante")
    OrdenSalidaDevolucionResponse toResponse(OrdenSalidaDevolucionDTO dto);

    /**
     * Formatea el código del comprobante.
     * Concatena: numeroSerie + '-' + numeroComprobante (rellenado con ceros hasta 8
     * caracteres)
     * 
     * @param dto el DTO completo para acceder a numeroSerie y numeroComprobante
     * @return el código formateado
     */
    @Named("formatearCodigoComprobante")
    default String formatearCodigoComprobante(OrdenSalidaDevolucionDTO dto) {
        log.info("🔧 @Named - Iniciando formateo de codigoComprobante");
        log.info("🔧 DTO recibido - numeroSerie: '{}', numeroComprobante: '{}'",
                dto.getNumeroSerie(), dto.getNumeroComprobante());

        // Validar que numeroComprobante no sea null y no esté vacío
        if (dto.getNumeroComprobante() == null || dto.getNumeroComprobante().trim().isEmpty()) {
            log.warn("⚠️ numeroComprobante es null o vacío, no se puede formatear");
            return null;
        }

        try {
            String numeroSerie = dto.getNumeroSerie() != null ? dto.getNumeroSerie().trim() : "";
            String numeroComprobante = dto.getNumeroComprobante().trim();

            log.info("🔧 Valores procesados - numeroSerie: '{}', numeroComprobante: '{}'",
                    numeroSerie, numeroComprobante);

            // Validar que numeroComprobante sea un número válido
            int numeroComprobanteInt = Integer.parseInt(numeroComprobante);

            // Rellenar con ceros hasta 8 caracteres
            String numeroComprobanteFormateado = String.format("%08d", numeroComprobanteInt);

            // Concatenar: numeroSerie + '-' + numeroComprobante
            String codigoComprobante = numeroSerie + "-" + numeroComprobanteFormateado;

            log.info("🔧 Código comprobante formateado: '{}'", codigoComprobante);

            log.info("✅ @Named completado exitosamente");
            return codigoComprobante;

        } catch (NumberFormatException e) {
            log.error("❌ Error al parsear numeroComprobante '{}': {}",
                    dto.getNumeroComprobante(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ Error inesperado en formatearCodigoComprobante: {}", e.getMessage());
            return null;
        }
    }
}