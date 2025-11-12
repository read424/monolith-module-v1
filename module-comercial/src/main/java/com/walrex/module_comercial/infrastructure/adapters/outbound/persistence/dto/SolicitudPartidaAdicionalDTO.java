package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO enriquecido para partidas adicionales afectadas por solicitud de cambio.
 * Contiene los datos necesarios para persistir en la tabla solicitudes_partidas_adicionales.
 *
 * Este DTO sirve como intermediario entre la capa de dominio y la capa de persistencia,
 * facilitando el mapeo hacia la entidad SolicitudPartidaAdicionalEntity.
 *
 * Ubicación: infrastructure/adapters/outbound/persistence/dto
 * Propósito: DTO de persistencia para partidas adicionales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPartidaAdicionalDTO {

    // Relación con la solicitud principal
    private Integer idSolicitud;  // FK a solicitudes_cambio_servicios_partidas

    // Datos de la partida adicional
    private Integer idPartida;
    private String codPartida;  // Código de la partida (para referencia)

    // Control y estado
    private Integer status;  // 1: Activo, 0: Inactivo
    private Integer aprobado;  // 0: No aprobado, 1: Aprobado

    // Auditoría
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}