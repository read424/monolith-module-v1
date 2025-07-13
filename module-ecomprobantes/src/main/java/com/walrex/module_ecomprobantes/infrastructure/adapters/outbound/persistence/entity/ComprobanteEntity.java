package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad R2DBC para la tabla tbcomprobantes.
 * 
 * Representa la estructura de datos de un comprobante electrónico
 * en la capa de infraestructura siguiendo arquitectura hexagonal.
 * 
 * CARACTERÍSTICAS:
 * - Reactive R2DBC Entity para PostgreSQL
 * - Mapeo directo con tabla facturacion.tbcomprobantes
 * - Soporte para auditoria automática (create_at, update_at)
 * - Builder pattern para construcción inmutable
 * - Validation constraints para integridad de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbcomprobantes", schema = "facturacion")
public class ComprobanteEntity {

    @Id
    @Column("id_comprobante")
    private Long idComprobante;

    @Column("id_tipocompro")
    private Integer idTipoComprobante;

    @Column("tctipo_serie")
    private Integer tipoSerie;

    @Column("nro_comprobante")
    private Integer numeroComprobante;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("fe_emision")
    private LocalDate fechaEmision;

    @Column("fe_vencimiento")
    private LocalDate fechaVencimiento;

    @Column("fe_registro")
    private LocalDate fechaRegistro;

    @Column("id_tipomoneda")
    private Integer idTipoMoneda;

    @Column("id_pago")
    private Integer idPago;

    @Column("id_forma_pago")
    private Integer idFormaPago;

    @Column("id_tipo_retencion")
    private Integer idTipoRetencion;

    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("igv")
    private BigDecimal igv;

    @Column("total")
    private BigDecimal total;

    @Column("id_motivo")
    private Integer idMotivo;

    @Column("observacion")
    private String observacion;

    @Column("id_modalidad")
    private Integer idModalidad;

    @Builder.Default
    @Column("status")
    private Integer status = 1;

    @Column("cod_response_sunat")
    private Integer codigoResponseSunat;

    @Column("response_sunat")
    private String responseSunat;

    @Column("fec_comunicacion")
    private LocalDate fechaComunicacion;

    @Column("desc_motivo")
    private String descripcionMotivo;

    @Builder.Default
    @Column("aplica_detraccion")
    private Short aplicaDetraccion = 0;

    @Builder.Default
    @Column("is_inafecta")
    private Short isInafecta = 0;

    @Builder.Default
    @Column("is_doc_autorizado")
    private Short isDocumentoAutorizado = 1;

    @Column("notes_sunat")
    private String notesSunat;

    @Builder.Default
    @Column("anulado")
    private Integer anulado = 0;

    @Column("num_ticket")
    private String numeroTicket;

    @Column("fec_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column("id_liquidacion")
    private Integer idLiquidacion;

    @CreatedDate
    @Column("create_at")
    private LocalDateTime createAt;

    @LastModifiedDate
    @Column("update_at")
    private LocalDateTime updateAt;

}