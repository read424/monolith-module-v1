package com.walrex.module_comercial.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para OrdenProduccion.
 */
public record OrdenProduccionResponseDTO(
    Integer idOrdenProduccion,
    String codOrdenProduccion,
    Integer idOrdenIngreso,
    Integer idArticulo,
    Integer idRuta,
    Integer idColor,
    Integer idReceta,
    String descArticulo,
    String nuComprobante,
    String lote,
    Integer nuRollos,
    Integer antipilling,
    String revirado,
    String kilaje,
    String complementos,
    String observacion,
    String opciones,
    LocalDateTime fecRegistro,
    Integer status,
    BigDecimal ancho,
    BigDecimal densidad,
    BigDecimal rendimiento,
    Integer idTipo,
    Integer idEncogimiento,
    Integer idOrden,
    Integer idDetOs,
    LocalDate fecProgramado,
    Boolean parametrosManuales,
    LocalDateTime createAt,
    LocalDateTime updateAt,
    BigDecimal encogimientoLargo,
    Boolean conComplementos
) {}
