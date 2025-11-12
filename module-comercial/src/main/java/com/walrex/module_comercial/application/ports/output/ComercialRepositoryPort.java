package com.walrex.module_comercial.application.ports.output;

import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.OrdenProduccionPartidaDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.PartidasStatusDespachoDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.ProcesosPartidaDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Port de salida para operaciones complejas de repositorio en el módulo comercial.
 * Define contratos para consultas personalizadas que no pueden ser manejadas por R2dbcRepository.
 *
 * @author Sistema
 * @version 0.0.1-SNAPSHOT
 */
public interface ComercialRepositoryPort {
    /**
     * Obtiene el estado de las partidas con consultas complejas
     *
     * @param idPartida ID de la orden de producción
     * @return Flux con mapas conteniendo el estado de las partidas
     */
    Mono<OrdenProduccionPartidaDTO> getInfoOrdenProduccionPartida(Integer idPartida);

    /**
     * Obtiene el estado de las partidas con consultas complejas
     *
     * @param idPartida ID de la orden de producción
     * @return Flux con mapas conteniendo el estado de las partidas
     */
    Flux<ProcesosPartidaDTO> getProcesosPartidaStatus(Integer idPartida);

    /**
     * Obtiene partidas con orden de produccion que esten despachadas
     * @param idOrdenProduccion
     * @return
     */
    Flux<PartidasStatusDespachoDTO> getStatusDespachoPartidas(Integer idOrdenProduccion);
}
