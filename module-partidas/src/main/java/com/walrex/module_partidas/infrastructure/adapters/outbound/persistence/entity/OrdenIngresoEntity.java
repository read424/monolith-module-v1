package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla almacenes.ordeningreso
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.ordeningreso")
public class OrdenIngresoEntity {

    @Id
    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("id_motivo")
    private Integer idMotivo;

    @Column("id_origen")
    private Integer idOrigen;

    @Column("id_comprobante")
    private Integer idComprobante;

    @Column("nu_comprobante")
    private String nuComprobante;

    @Column("observacion")
    private String observacion;

    @Column("fec_ingreso")
    private LocalDate fecIngreso;

    @Column("fec_ref")
    private LocalDate fecRef;

    @Column("fec_registro")
    private OffsetDateTime fecRegistro;

    @Column("status")
    private Integer status;

    @Column("nu_serie")
    private String nuSerie;

    @Column("cod_ingreso")
    private String codIngreso;

    @Column("id_almacen")
    private Integer idAlmacen;

    @Column("id_orden")
    private Integer idOrden;

    @Column("id_centro")
    private Integer idCentro;

    @Column("id_ubicacion")
    private Integer idUbicacion;

    @Column("id_maquina")
    private Integer idMaquina;

    @Column("id_naturaleza")
    private Integer idNaturaleza;

    @Column("descripcion")
    private String descripcion;

    @Column("comprobante_ref")
    private String comprobanteRef;

    @Column("condicion")
    private Integer condicion;

    @Column("status_bk")
    private Integer statusBk;

    @Column("id_proceso")
    private Integer idProceso;

    @Column("id_motivo_rechazo")
    private Integer idMotivoRechazo;

    @Column("upate_at")
    private OffsetDateTime upateAt;

    @Column("id_orden_serv")
    private Integer idOrdenServ;
}
