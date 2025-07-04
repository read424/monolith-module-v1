package com.walrex.module_almacen.domain.model.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

/**
 * DTO que representa un rollo disponible para devolución
 * Value Object del dominio siguiendo el patrón DDD
 */
@Data
@Builder
public class RolloDisponibleDevolucionDTO {
    // Datos del rollo de ingreso
    private Integer idOrdeningreso;
    private Integer idDetordeningreso;
    private Integer idDetordeningresopeso;
    private String codigoOrdenIngreso;
    private LocalDate fechaIngreso;
    private String numComprobante;
    private Integer statusRolloIngreso;

    // Datos del artículo
    private Integer idArticulo;

    // Datos del rollo
    private String codRollo;
    private Double pesoRollo;

    // Datos del rollo en almacén
    private Integer idOrdeningresoAlmacen;
    private Integer idDetordeningresoAlmacen;
    private Integer idDetordeningresopesoAlmacen;
    private Integer statusRolloAlmacen;

    // Datos del almacén
    private String codIngresoAlmacen;
    private Integer idIngresoAlmacen;
    private String noAlmacen;

    // Datos de partida
    private Integer idPartida;
    private String codPartida;
    private String sinCobro;
    private Integer idDetallePartida;
    private Integer statusRollPartida;
}
