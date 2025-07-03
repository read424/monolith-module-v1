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
    private Integer idDetordeningresopeso;
    private Integer idDetordeningreso;
    private Integer idOrdeningreso;
    private String codigoOrdenIngreso;
    private LocalDate fechaIngreso;
    private String numComprobante;
    private Integer statusRolloIngreso;

    // Datos del artículo
    private Integer idArticulo;

    // Datos del rollo
    private String codRollo;

    // Datos del rollo en almacén
    private Integer statusRolloAlmacen;

    // Datos de partida
    private Integer idDetallePartida;
    private Integer idPartida;
    private String codPartida;
    private Integer statusRollPartida;

    // Datos del almacén
    private String codIngresoAlmacen;
    private Integer idIngresoAlmacen;
    private String noAlmacen;
}