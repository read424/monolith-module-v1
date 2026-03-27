package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.SessionPesajeActiva;
import com.walrex.module_almacen.domain.model.dto.RolloPesadoDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionArticuloPesajeOutputPort {
    /** Devuelve la sesión de pesaje por id_detordeningreso, o Mono.empty() si no existe. */
    Mono<SessionPesajeActiva> findStatusByIdDetOrdenIngreso(Integer idDetOrdenIngreso);

    /** Ejecuta el query complejo y retorna los datos agregados del artículo con su sesión. */
    Mono<ArticuloPesajeSession> getArticuloWithSessionDetail(Integer idDetOrdenIngreso);

    /** Inserta un nuevo registro en session_pesaje_activa. */
    Mono<Void> insertSession(Integer idDetOrdenIngreso, Integer cntRollos, Double totKg);

    /** Actualiza el status de la sesión a '0' (completada) por id. */
    Mono<Void> updateSessionStatusToCompleted(Integer sessionId);

    /** Reactiva la sesión a status '1' por id. */
    Mono<Void> updateSessionStatusToActive(Integer sessionId);

    /** Retorna los rollos pesados de un artículo ordenados del más reciente al más antiguo. */
    Flux<RolloPesadoDTO> findRollosByIdDetOrdenIngreso(Integer idDetOrdenIngreso);
}
