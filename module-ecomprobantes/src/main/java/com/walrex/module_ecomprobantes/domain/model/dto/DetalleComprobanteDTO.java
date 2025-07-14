package com.walrex.module_ecomprobantes.domain.model.dto;

import java.math.BigDecimal;

import lombok.*;

/**
 * DTO para transferencia de datos de detalles de comprobantes
 * 
 * CARACTERÍSTICAS:
 * - Representa una línea/item de un comprobante
 * - Mapea directamente con DetalleComprobanteEntity
 * - Contiene información de producto, cantidad, precios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleComprobanteDTO {

    private Long idDetalleComprobante;
    private Long idComprobante;
    private Integer idProducto;
    private Integer idOrdenSalida;

    private BigDecimal cantidad;

    private BigDecimal precio;

    private BigDecimal precioOriginal;

    private BigDecimal subtotal;

    private String observacion;
    private Integer idDetalleOrden;

    private BigDecimal peso;

    private Integer idUnidad;

    private Short idTipoServicio;

}
