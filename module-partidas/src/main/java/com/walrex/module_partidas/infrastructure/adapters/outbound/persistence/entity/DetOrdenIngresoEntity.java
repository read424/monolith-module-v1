package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla almacenes.detordeningreso
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.detordeningreso")
public class DetOrdenIngresoEntity {

    @Id
    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_articulo")
    private Integer idArticulo;

    @Column("id_unidad")
    private Integer idUnidad;

    @Column("lote")
    private String lote;

    @Column("peso_ref")
    private BigDecimal pesoRef;

    @Column("peso_alm")
    private BigDecimal pesoAlm;

    @Column("peso_dif")
    private BigDecimal pesoDif;

    @Column("nu_rollos")
    private BigDecimal nuRollos;

    @Column("observacion")
    private String observacion;

    @Column("cod_os")
    private String codOs;

    @Column("id_tipo_producto")
    private Integer idTipoProducto;

    @Column("costo_compra")
    private BigDecimal costoCompra;

    @Column("id_orden")
    private Integer idOrden;

    @Column("id_tipo_comprobante")
    private Integer idTipoComprobante;

    @Column("id_comprobante")
    private Integer idComprobante;

    @Column("status")
    private Integer status;

    @Column("id_kardex")
    private Integer idKardex;

    @Column("add_orphan")
    private Integer addOrphan;

    @Column("id_moneda")
    private Integer idMoneda;

    @Column("excento_imp")
    private Integer excentoImp;

    @Column("id_tipo")
    private Integer idTipo;

    @Column("peso_merma")
    private BigDecimal pesoMerma;

    @Column("peso_percha")
    private BigDecimal pesoPercha;

    @Column("peso_acabado")
    private BigDecimal pesoAcabado;

    @Column("peso_devolucion")
    private BigDecimal pesoDevolucion;
}
