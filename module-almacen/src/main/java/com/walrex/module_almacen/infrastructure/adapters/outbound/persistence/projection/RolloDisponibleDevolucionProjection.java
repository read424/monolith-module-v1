package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDate;

import lombok.*;

/**
 * Projection para mapear el resultado de la consulta de rollos disponibles para
 * devolución
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloDisponibleDevolucionProjection {
    // Datos del rollo de ingreso
    private Integer idDetordeningresopeso;
    private Integer idDetordeningreso;
    private Integer idOrdeningreso;
    private String codIngreso;
    private LocalDate fechaIngreso;
    private String nuComprobante;
    private Integer statusIng;

    // Datos del artículo
    private Integer idArticulo;

    // Datos del rollo
    private String codRollo;

    // Datos del rollo en almacén
    private Integer statusAlmacen;

    // Datos de partida
    private Integer idDetPartida;
    private Integer idPartida;
    private String codPartida;
    private Integer statusRollPartida;

    // Datos del almacén
    private String codIngresoAlmacen;
    private Integer idAlmacen;
    private String noAlmacen;
}