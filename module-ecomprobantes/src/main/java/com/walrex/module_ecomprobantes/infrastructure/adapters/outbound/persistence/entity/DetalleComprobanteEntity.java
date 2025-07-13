package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad R2DBC para la tabla tbdet_comprobantes.
 * 
 * Representa el detalle de líneas de un comprobante electrónico
 * 
 * CARACTERÍSTICAS:
 * - Reactive R2DBC Entity para PostgreSQL
 * - Mapeo directo con tabla facturacion.tbdet_comprobantes
 * - Relación con ComprobanteEntity a través de idComprobante
 * - Soporte para auditoria automática (create_at, update_at)
 * - Builder pattern para construcción inmutable
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbdet_comprobantes", schema = "facturacion")
public class DetalleComprobanteEntity {

    @Id
    @Column("id_det_comprobante")
    private Long idDetalleComprobante;

    @Column("id_comprobante")
    private Long idComprobante;

    @Column("id_producto")
    private Integer idProducto;

    @Column("id_ordensalida")
    private Integer idOrdenSalida;

    @Column("cantidad")
    private BigDecimal cantidad;

    @Column("precio")
    private BigDecimal precio;

    @Column("preciooriginal")
    private BigDecimal precioOriginal;

    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("no_observacion")
    private String observacion;

    @Column("id_detalle_orden")
    private Integer idDetalleOrden;

    @Builder.Default
    @Column("peso")
    private BigDecimal peso = BigDecimal.ZERO;

    @Column("id_unidad")
    private Integer idUnidad;

    @Builder.Default
    @Column("id_tipo_servicio")
    private Short idTipoServicio = 1;

    @CreatedDate
    @Column("create_at")
    private LocalDateTime createAt;

    @LastModifiedDate
    @Column("update_at")
    private LocalDateTime updateAt;
}