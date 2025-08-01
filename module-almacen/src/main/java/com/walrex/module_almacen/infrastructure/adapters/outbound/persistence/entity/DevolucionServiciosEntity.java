package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad para el header de devoluciones de servicios
 * Tabla: almacenes.devolucion_servicios
 * Almacena información general de las devoluciones de servicios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.devolucion_servicios")
public class DevolucionServiciosEntity {

    @Id
    @Column("id_devolucion")
    private Long id;

    @Column("id_ordensalida")
    private Integer idOrdenSalida;

    @Column("id_motivo")
    private Integer idMotivo;

    @Column("motivo_comprobante")
    private Integer idMotivoComprobante;

    @Column("id_comprobante")
    private Integer idComprobante;

    @Column("id_empresa_transp")
    private Integer idEmpresaTransp;

    @Column("id_modalidad")
    private Integer idModalidad;

    @Column("id_conductor")
    private Integer idConductor;

    @Column("num_placa")
    private String numPlaca;

    @Column("id_llegada")
    private Integer idLlegada;

    @Column("fec_entrega")
    private LocalDate fecEntrega;

    private String observacion;

    @Column("id_usuario")
    private Integer idUsuario;

    private Integer entregado;

    private Integer status;

    @Column("create_at")
    private OffsetDateTime createAt;

    @Column("update_at")
    private OffsetDateTime updateAt;
}
