package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("almacenes.almacen")
public class AlmacenEntity {
    @Id
    @Column("id_almacen")
    private Long id;

    @Column("id_tipoalmacen")
    private Integer tipo_almacen;
    private Integer id_encargado;
    private String cod_almacen;
    private String no_almacen;
    private String alias_almacen;
    private String desc_almacen;
    private String anexo_almacen;
    private String prefijo_entrada;
    private String prefijo_salida;
    private String prefijo_ajuste;
    private Integer corre_entrada;
    private Integer corre_salida;
    private Integer corre_ajuste;
    private Date fec_registro;
    private Integer status;
    private Integer isoc;
    private Integer isdespacho;
    private Integer isvale;
    private Integer id_ubicacion;
    private Integer isdevolucion;
    private Integer id_proceso;
}
