package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.ordensalida")
public class OrdenSalidaEntity {
    @Id
    private Long id_ordensalida;
    private Integer id_motivo;
    private Integer is_interno;
    private Integer id_tipo_comprobante;
    private String num_comprobante;
    @Column("id_almacen_origen")
    private Integer id_store_source;
    @Column("id_almacen_destino")
    private Integer id_store_target;
    @Column("fec_registro")
    private OffsetDateTime create_at;
    private Integer id_usuario;
    private LocalDate fec_entrega;
    private Integer id_user_entrega;
    private Integer entregado;
    private Integer id_documento_ref;
    private String cod_salida;
    private Integer status;
    private Integer id_cliente;
    private Integer id_requerimiento;
    private Integer id_supervisor;
    private String observacion;
    private Long correlativo_motivo;
    private Integer id_usuario_declara;
    private LocalDateTime update_at;
}
