package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

/**
 * Proyección para el detalle de ingreso con rollos
 * Basada en el SQL principal con JOINs múltiples
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleIngresoProjection {

    /**
     * ID del artículo
     */
    private Integer idArticulo;

    /**
     * Código del artículo
     */
    private String codArticulo;

    /**
     * Descripción del artículo
     */
    private String descArticulo;

    /**
     * ID del detalle de orden de ingreso
     */
    private Integer idDetordeningreso;

    /**
     * Lote del detalle de orden de ingreso
    */
    private String lote;

    /**
     * ID de la orden de ingreso
     */
    private Integer idOrdeningreso;

    /**
     * ID del tipo de producto
     */
    private Integer idTipoProducto;

    /**
     * ID de la unidad
     */
    private Integer idUnidad;

    /**
     * Abreviatura de la unidad
     */
    private String abrevUnidad;

    /**
     * Cantidad de rollos
     */
    private Integer cntRollos;
}
