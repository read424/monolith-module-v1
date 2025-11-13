package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.SolicitudPartidaAdicionalEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC reactivo para SolicitudPartidaAdicionalEntity.
 * Proporciona operaciones CRUD reactivas para partidas adicionales afectadas por solicitudes de cambio.
 */
@Repository
public interface SolicitudPartidaAdicionalR2dbcRepository
        extends ReactiveCrudRepository<SolicitudPartidaAdicionalEntity, Integer> {

    /**
     * Busca todas las partidas adicionales de una solicitud específica.
     *
     * @param idSolicitud ID de la solicitud de cambio principal
     * @return Flux con las partidas adicionales de la solicitud
     */
    Flux<SolicitudPartidaAdicionalEntity> findByIdSolicitud(Integer idSolicitud);

    /**
     * Busca partidas adicionales por ID de partida.
     *
     * @param idPartida ID de la partida
     * @return Flux con las partidas adicionales asociadas a la partida
     */
    Flux<SolicitudPartidaAdicionalEntity> findByIdPartida(Integer idPartida);

    /**
     * Busca partidas adicionales por estado.
     *
     * @param status Estado (1 = activo, 0 = inactivo)
     * @return Flux con las partidas adicionales filtradas por estado
     */
    Flux<SolicitudPartidaAdicionalEntity> findByStatus(Integer status);

    /**
     * Busca partidas adicionales por solicitud y estado de aprobación.
     *
     * @param idSolicitud ID de la solicitud
     * @param aprobado Estado de aprobación (0 = no aprobado, 1 = aprobado)
     * @return Flux con las partidas adicionales filtradas
     */
    Flux<SolicitudPartidaAdicionalEntity> findByIdSolicitudAndAprobado(Integer idSolicitud, Integer aprobado);

    /**
     * Cuenta las partidas adicionales de una solicitud.
     *
     * @param idSolicitud ID de la solicitud
     * @return Mono con el total de partidas adicionales
     */
    Mono<Long> countByIdSolicitud(Integer idSolicitud);

    /**
     * Busca partidas adicionales activas y aprobadas de una solicitud.
     *
     * @param idSolicitud ID de la solicitud
     * @return Flux con las partidas adicionales aprobadas y activas
     */
    @Query("SELECT * FROM produccion.solicitudes_partidas_adicionales " +
           "WHERE id_solicitud = :idSolicitud AND status = 1 AND aprobado = 1 " +
           "ORDER BY create_at DESC")
    Flux<SolicitudPartidaAdicionalEntity> findActiveApprovedByIdSolicitud(Integer idSolicitud);

    /**
     * Elimina todas las partidas adicionales de una solicitud.
     * Útil para operaciones de limpieza o actualización masiva.
     *
     * @param idSolicitud ID de la solicitud
     * @return Mono<Void> que indica la finalización de la operación
     */
    Mono<Void> deleteByIdSolicitud(Integer idSolicitud);

    /**
     * Verifica si existe una partida adicional para una solicitud y partida específicas.
     *
     * @param idSolicitud ID de la solicitud
     * @param idPartida ID de la partida
     * @return Mono<Boolean> indicando si existe
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
           "FROM produccion.solicitudes_partidas_adicionales " +
           "WHERE id_solicitud = :idSolicitud AND id_partida = :idPartida")
    Mono<Boolean> existsBySolicitudAndPartida(Integer idSolicitud, Integer idPartida);

    /**
     * Obtiene partidas adicionales con información de solicitud (JOIN).
     * Esta query más compleja permite obtener datos relacionados en una sola consulta.
     *
     * @param idPartida ID de la partida
     * @return Flux con las partidas adicionales y sus solicitudes relacionadas
     */
    @Query("SELECT spa.* FROM produccion.solicitudes_partidas_adicionales spa " +
           "INNER JOIN produccion.solicitudes_cambio_servicios_partidas scsp " +
           "ON spa.id_solicitud = scsp.id " +
           "WHERE spa.id_partida = :idPartida AND spa.status = 1 AND scsp.status = 1 " +
           "ORDER BY spa.create_at DESC")
    Flux<SolicitudPartidaAdicionalEntity> findWithActiveSolicitudByPartida(Integer idPartida);
}