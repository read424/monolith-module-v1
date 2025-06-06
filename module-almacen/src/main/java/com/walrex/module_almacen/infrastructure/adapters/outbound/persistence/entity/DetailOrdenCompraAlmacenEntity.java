package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table("logistica.detordencompra")
public class DetailOrdenCompraAlmacenEntity {
    @Id
    @Column("id_detordencompra")
    private Long id;
    @Column("id_orden")
    private Integer idOrden;
    @Column("id_articulo")
    private Integer idArticulo;
    private Double cantidad;
    private BigDecimal costo;
    private BigDecimal total;
    @Column("exento_imp")
    private Integer exentoImp;
    @Column("id_moneda")
    private Integer idMoneda;
    @Column("change_precio")
    private Boolean changePrecio;
    @Column("mto_ult_compra")
    private BigDecimal mtoUltCompra;
    @Column("precio_aprobado")
    private Boolean precioAprobado;
    private BigDecimal saldo;
    @Column("id_categoria_oc")
    private Integer idCategoriaOc;
    @Column("id_subcategoria_oc")
    private Integer idSubcategoriaOc;
    @Column("id_tipo_subcategoria_oc")
    private Integer idTipoSubcategoriaOc;
    @Column("id_maquina")
    private Integer idMaquina;
    @Column("id_detalle_categ_oc")
    private Integer idDetalleCategOc;
}
