package com.walrex.module_comercial.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para OrdenProduccion.
 * Representa la entidad de negocio en la capa de dominio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenProduccion {
    Integer idOrdenProduccion;
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
    LocalDateTime fecRegistro;
    Integer status;
    BigDecimal ancho;
    BigDecimal densidad;
    BigDecimal rendimiento;
    Integer idTipo;
    Integer idEncogimiento;
    Integer idOrden;
    Integer idDetOs;
    LocalDate fecProgramado;
    Boolean parametrosManuales;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    BigDecimal encogimientoLargo;
    Boolean conComplementos;
}