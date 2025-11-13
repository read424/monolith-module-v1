package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.SolicitudCambioServicioPartidaEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC reactivo para SolicitudCambioServicioPartidaEntity.
 * Proporciona operaciones CRUD reactivas y consultas personalizadas para solicitudes de cambio.
 */
@Repository
public interface SolicitudCambioServicioPartidaR2dbcRepository
        extends ReactiveCrudRepository<SolicitudCambioServicioPartidaEntity, Integer> {

    /**
     * Busca solicitudes de cambio por ID de partida.
     *
     * @param idPartida ID de la partida
     * @return Flux con las solicitudes de cambio de la partida
     */
    Flux<SolicitudCambioServicioPartidaEntity> findByIdPartida(Integer idPartida);

    /**
     * Busca solicitudes de cambio por ID de orden de producción (nueva).
     *
     * @param idOrdenproduccion ID de la orden de producción
     * @return Flux con las solicitudes de cambio de la orden
     */
    Flux<SolicitudCambioServicioPartidaEntity> findByIdOrdenproduccion(Integer idOrdenproduccion);

    /**
     * Busca solicitudes de cambio por estado.
     *
     * @param status Estado de la solicitud (1 = activo, 0 = inactivo)
     * @return Flux con las solicitudes filtradas por estado
     */
    Flux<SolicitudCambioServicioPartidaEntity> findByStatus(Integer status);

    /**
     * Busca solicitudes de cambio pendientes de aprobación.
     *
     * @return Flux con las solicitudes pendientes de aprobación
     */
    @Query("SELECT * FROM produccion.solicitudes_cambio_servicios_partidas " +
           "WHERE por_aprobar = 1 AND status = 1 " +
           "ORDER BY create_at DESC")
    Flux<SolicitudCambioServicioPartidaEntity> findPendientesAprobacion();

    /**
     * Busca solicitudes aprobadas por ID de usuario.
     *
     * @param idUsuario ID del usuario que creó las solicitudes
     * @return Flux con las solicitudes aprobadas del usuario
     */
    @Query("SELECT * FROM produccion.solicitudes_cambio_servicios_partidas " +
           "WHERE id_usuario = :idUsuario AND aprobado = 1 AND status = 1 " +
           "ORDER BY create_at DESC")
    Flux<SolicitudCambioServicioPartidaEntity> findApprovedByUsuario(Integer idUsuario);

    /**
     * Busca solicitudes de cambio por partida y estado de aprobación.
     *
     * @param idPartida ID de la partida
     * @param aprobado Estado de aprobación (0 = no aprobado, 1 = aprobado)
     * @return Flux con las solicitudes filtradas
     */
    Flux<SolicitudCambioServicioPartidaEntity> findByIdPartidaAndAprobado(Integer idPartida, Integer aprobado);

    /**
     * Cuenta las solicitudes pendientes de aprobación.
     *
     * @return Mono con el total de solicitudes pendientes
     */
    @Query("SELECT COUNT(*) FROM produccion.solicitudes_cambio_servicios_partidas " +
           "WHERE por_aprobar = 1 AND status = 1")
    Mono<Long> countPendientesAprobacion();

    /**
     * Verifica si existe una solicitud activa para una partida específica.
     *
     * @param idPartida ID de la partida
     * @return Mono<Boolean> indicando si existe una solicitud activa
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
           "FROM produccion.solicitudes_cambio_servicios_partidas " +
           "WHERE id_partida = :idPartida AND status = 1")
    Mono<Boolean> existsActiveSolicitudByPartida(Integer idPartida);
}