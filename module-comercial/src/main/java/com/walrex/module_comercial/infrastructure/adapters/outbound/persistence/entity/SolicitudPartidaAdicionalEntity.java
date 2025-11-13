package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia R2DBC para partidas adicionales afectadas por solicitud de cambio.
 * Mapea a la tabla produccion.solicitudes_partidas_adicionales.
 *
 * Representa las partidas adicionales que son afectadas por una solicitud de cambio de servicio.
 * Relación N:1 con SolicitudCambioServicioPartidaEntity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table(name = "solicitudes_partidas_adicionales", schema = "produccion")
public class SolicitudPartidaAdicionalEntity {

    @Id
    @Column("id")
    private Integer id;

    // Relación con la solicitud principal (Foreign Key)
    @Column("id_solicitud")
    private Integer idSolicitud;

    // Datos de la partida adicional
    @Column("id_partida")
    private Integer idPartida;

    // Estado y control de aprobación
    @Column("status")
    private Integer status;

    @Column("aprobado")
    private Integer aprobado;

    // Auditoría
    @Column("create_at")
    private LocalDateTime createAt;

    @Column("update_at")
    private LocalDateTime updateAt;
}