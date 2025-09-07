package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

/**
 * Projection para consultar información completa de una orden de ingreso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenIngresoCompletaProjection {

    /**
     * ID de la orden de ingreso
     */
    private Integer idOrdeningreso;

    /**
     * ID del cliente
     */
    private Integer idCliente;

    /**
     * Código de ingreso generado por trigger
     */
    private String codIngreso;

    /**
     * ID del almacén
     */
    private Integer idAlmacen;
}
