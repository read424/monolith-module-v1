package com.walrex.module_comercial.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para operaciones de OrdenProduccion.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdenProduccionRequestDTO{
    String codOrdenProduccion;

    Integer idOrdenIngreso;

    Integer idArticulo;

    Integer idRuta;

    Integer idColor;

    Integer idReceta;

    String descArticulo;

    String nuComprobante;

    String lote;

    Integer nuRollos;

    Integer antipilling;

    String revirado;

    String kilaje;

    String complementos;

    String observacion;

    String opciones;

    BigDecimal ancho;

    BigDecimal densidad;

    BigDecimal rendimiento;

    Integer idTipo;

    Integer idEncogimiento;

    Integer idOrden;

    Integer idDetOs;

    LocalDate fecProgramado;

    Boolean parametrosManuales;

    BigDecimal encogimientoLargo;

    Boolean conComplementos;
}
