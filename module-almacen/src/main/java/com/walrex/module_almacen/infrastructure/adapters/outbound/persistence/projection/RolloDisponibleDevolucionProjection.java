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
    private Integer idOrdeningreso;
    private Integer idDetordeningreso;
    private Integer idDetordeningresopeso;
    private String codIngreso;
    private LocalDate fechaIngreso;
    private String nuComprobante;
    private Integer statusIng;

    // Datos del artículo
    private Integer idArticulo;

    // Datos del rollo
    private String codRollo;
    private Double pesoRollo;

    // Datos del rollo en almacén
    private Integer idOrdeningresoAlmacen;
    private Integer idDetordeningresoAlmacen;
    private Integer idDetordeningresopesoAlmacen;
    private Integer statusAlmacen;

    // Datos del almacén
    private String codIngresoAlmacen;
    private Integer idAlmacen;
    private String noAlmacen;

    // Datos de partida
    private Integer idPartida;
    private String codPartida;
    private Integer sinCobro;
    private Integer idDetPartida;
    private Integer statusRollPartida;
}
