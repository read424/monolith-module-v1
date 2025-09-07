package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

/**
 * Proyección para los rollos disponibles
 * Basada en el SQL de detalle de rollos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRolloProjection {

    /**
     * Código del rollo
     */
    private String codRollo;

    /**
     * Indica si el rollo está despachado
     */
    private Boolean despacho;

    /**
     * Estado del rollo (disabled)
     */
    private String disabled;

    /**
     * ID del almacén
     */
    private Integer idAlmacen;

    /**
     * ID del detalle de partida
     */
    private Integer idDetPartida;

    /**
     * ID del ingreso al almacén
     */
    private Integer idIngresoAlmacen;

    /**
     * ID del ingreso de peso
     */
    private Integer idIngresopeso;

    /**
     * ID de la orden de ingreso
     */
    private Integer idOrdeningreso;

    /**
     * ID del rollo de ingreso
     */
    private Integer idRolloIngreso;

    /**
     * Indica si es rollo padre
     */
    private Integer isParentRollo;

    /**
     * Nombre del almacén
     */
    private String noAlmacen;

    /**
     * Número de rollos hijos
     */
    private Integer numChildRoll;

    /**
     * Peso acabado
     */
    private Double pesoAcabado;

    /**
     * Peso del rollo
     */
    private Double pesoRollo;

    /**
     * Peso saldo
     */
    private Double pesoSaldo;

    /**
     * Peso de salida
     */
    private Double pesoSalida;

    /**
     * Estado del rollo
     */
    private Integer status;
}
