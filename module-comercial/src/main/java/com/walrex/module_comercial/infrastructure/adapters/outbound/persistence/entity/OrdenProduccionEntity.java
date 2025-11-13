package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia R2DBC para OrdenProduccion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table(name = "tborden_produccion", schema = "comercial")
public class OrdenProduccionEntity{
    @Id
    @Column("id_ordenproduccion")
    Integer idOrdenProduccion;

    @Column("cod_ordenproduccion")
    String codOrdenProduccion;

    @Column("id_ordeningreso")
    Integer idOrdenIngreso;

    @Column("id_articulo")
    Integer idArticulo;

    @Column("id_ruta")
    Integer idRuta;

    @Column("id_color")
    Integer idColor;

    @Column("id_receta")
    Integer idReceta;

    @Column("desc_articulo")
    String descArticulo;

    @Column("nu_comprobante")
    String nuComprobante;

    @Column("lote")
    String lote;

    @Column("nu_rollos")
    Integer nuRollos;

    @Column("antipilling")
    Integer antipilling;

    @Column("revirado")
    String revirado;

    @Column("kilaje")
    String kilaje;

    @Column("complementos")
    String complementos;

    @Column("observacion")
    String observacion;

    @Column("opciones")
    String opciones;

    @Column("fec_registro")
    LocalDateTime fecRegistro;

    @Column("status")
    Integer status;

    @Column("ancho")
    BigDecimal ancho;

    @Column("densidad")
    BigDecimal densidad;

    @Column("rendimiento")
    BigDecimal rendimiento;

    @Column("id_tipo")
    Integer idTipo;

    @Column("id_encogimiento")
    Integer idEncogimiento;

    @Column("id_orden")
    Integer idOrden;

    @Column("id_det_os")
    Integer idDetOs;

    @Column("fec_programado")
    LocalDate fecProgramado;

    @Column("parametros_manuales")
    Boolean parametrosManuales;

    @Column("create_at")
    LocalDateTime createAt;

    @Column("update_at")
    LocalDateTime updateAt;

    @Column("encogimiento_largo")
    BigDecimal encogimientoLargo;

    @Column("con_complementos")
    Boolean conComplementos;
}
