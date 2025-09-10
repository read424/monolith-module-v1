package com.walrex.module_partidas.domain.model.dto;

import java.util.List;
import java.util.Map;

import com.walrex.module_partidas.domain.model.ItemRollo;

import lombok.*;

/**
 * DTO para respuesta de ingreso a almacén con información detallada de rollos procesados
 * Incluye mapeo de órdenes de ingreso con cantidad de rollos procesados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoAlmacenDTO {

    /**
     * ID de la orden de ingreso
     */
    private Integer idOrdeningreso;

    /**
     * ID del cliente
     */
    private Integer idCliente;

    /**
     * Código de ingreso generado
     */
    private String codIngreso;

    /**
     * ID del almacén destino
     */
    private Integer idAlmacen;
    
    /**
     * ID del artículo
     */
    private Integer idArticulo;

    /**
     * ID de la unidad
     */
    private Integer idUnidad;

    /**
     * Cantidad total de rollos procesados
     */
    private Integer cntRollos;

    /**
     * Cantidad total de rollos en Almacen
    */
    private Integer cntRollosAlmacen;

    /**
     * Peso total de referencia
     */
    private Double pesoRef;

    /**
     * Lista de rollos procesados
     */
    private List<ItemRollo> rollos;

    /**
     * Mapeo de órdenes de ingreso con cantidad de rollos procesados
    */
    private Map<Integer, Integer> ingresos;
}
