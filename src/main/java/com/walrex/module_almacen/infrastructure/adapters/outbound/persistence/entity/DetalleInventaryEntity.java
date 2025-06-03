package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.detalle_inventario")
public class DetalleInventaryEntity {
    @Id
    @Column("id_lote")
    private Long idLote;
    @Column("id_articulo")
    private Integer idArticulo;
    private String lote;
    @Column("formato_date")
    private String formatoDate;
    @Column("fecha_vencimiento")
    private Date fechaVencimiento;
    @Column("id_almacen")
    private Integer idAlmacen;
    @Column("id_ubicacion")
    private Integer idUbicacion;
    private Double cantidad;
    @Column("cantidad_disponible")
    private Double cantidadDisponible;
    @Column("costo_compra")
    private BigDecimal costoCompra;
    @Column("precio_venta")
    private BigDecimal precioVenta;
    private Integer status;
    @Column("costo_consumo")
    private BigDecimal costoConsumo;
    @Column("fecha_ingreso")
    private OffsetDateTime fechaIngreso;
    @Column("id_detordeningreso")
    private Integer idDetordenIngreso;
    @Column("fec_ing_inv")
    private Date fecIngInv;
    @Column("id_moneda")
    private Integer idMoneda;
    @Column("excento_impuesto")
    private Integer excentoImpuesto;
}
