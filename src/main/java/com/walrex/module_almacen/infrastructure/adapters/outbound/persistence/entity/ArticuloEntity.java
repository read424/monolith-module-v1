package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("logistica.tbarticulos")
public class ArticuloEntity {
    private Long id_articulo;
    private Integer id_familia;
    private Integer id_grupo;
    private String cod_articulo;
    private String desc_articulo;
    private Integer id_medida;
    private Integer id_unidad;
    private Integer id_marca;
    private Integer id_componente;
    private Integer id_tipo_tela;
    private String galga;
    private String descripcion;
    private BigDecimal stock_min;
    private BigDecimal stock_max;
    private String msgstockminimo;
    private BigDecimal mto_compra;
    private String imagen;
    private LocalDate fec_ingreso;
    private Integer status;
    private Integer id_tipo_producto;
    private Integer id_unidad_consumo;
    private Integer method_venta;
    private Double mto_minimo;
    private Double mto_sugerido;
    private Double porc_ganancia;
    private Double mto_consumo;
    private Integer id_mezcla;
    private String porcentaje;
    private Integer id_moneda;
    private Integer id_tejido;
    private Boolean is_transformacion;
    private BigDecimal mto_ult_compra;
    private LocalDate fec_ult_compra;
    private Integer id_moneda_ult_compra;
    private Integer excento_ult_compra;
    private Integer excento_impuesto;
}
