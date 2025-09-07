package com.walrex.module_partidas.domain.model;

import java.util.List;

import lombok.*;

/**
 * Modelo de dominio para Detalle de Ingreso con Rollos
 * Representa la información de rollos disponibles para una partida en un
 * almacén específico
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleIngresoRollos {

    /**
     * Abreviatura de la unidad
     */
    private String abrevUnidad;

    /**
     * Cantidad de rollos
     */
    private Integer cntRollos;

    /**
     * Código del artículo
     */
    private String codArticulo;

    /**
     * Descripción del artículo
     */
    private String descArticulo;

    /**
     * ID del artículo
     */
    private Integer idArticulo;

    /**
     * Lote del detalle de orden de ingreso
    */
    private String lote;

    /**
     * ID del detalle de orden de ingreso
     */
    private List<Integer> idDetordeningreso;

    /**
     * ID de la orden de ingreso
     */
    private List<Integer> idOrdeningreso;

    /**
     * ID del tipo de producto
     */
    private Integer idTipoProducto;

    /**
     * ID de la unidad
     */
    private Integer idUnidad;

    /**
     * Lista de rollos disponibles
     */
    private List<ItemRollo> rollos;
}
