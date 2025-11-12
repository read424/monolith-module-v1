package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO enriquecido para solicitud de cambio de servicio de partida.
 * Contiene TODOS los datos necesarios para persistir en la tabla solicitudes_cambio_servicios_partidas.
 *
 * Este DTO sirve como intermediario entre la capa de dominio y la capa de persistencia,
 * conteniendo tanto los valores OLD (actuales en BD) como los valores NEW (propuestos).
 *
 * Ubicación: infrastructure/adapters/outbound/persistence/dto
 * Propósito: DTO de persistencia enriquecido con datos reales de BD
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCambioServicioPartidaDTO {

    private Integer idPartida;

    private Integer idOrdenproduccionOld;
    private Integer idOrdenOld;
    private Integer idDetOsOld;
    private Integer idPrecioOld;  // ID del precio (si existe en alguna tabla de precios)
    private Double precioOld;
    private Integer idGamaOld;
    private Integer idRutaOld;
    private String descArticuloOld;

    private Integer idOrdenproduccion;
    private Integer idOrden;
    private Integer idDetOs;
    private Integer idRuta;
    private Integer idGama;
    private Integer idPrecio;  // ID del precio nuevo (si existe)
    private Double precio;

    private Integer status;  // 1: Activo, 0: Inactivo
    private Integer aprobado;  // 0: No aprobado, 1: Aprobado
    private Integer porAprobar;  // 1: Pendiente de aprobación, 0: No requiere
    private Integer partidasAdicionales;  // 0: Solo partida actual, 1: Resto de Partidas Sin Despachar

    private Integer idUsuario;  // Usuario que crea la solicitud
    private Integer idUsuarioAutorizado;  // Usuario que autoriza (null inicialmente)

    private LocalDate fecRegistro;

    private List<SolicitudPartidaAdicionalDTO> partidasAdicionalesDTO;
}
