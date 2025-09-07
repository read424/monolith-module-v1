package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.AlmacenTachoResponse;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para consultar almacén tacho
 * Define el contrato para la consulta de partidas en almacén tacho
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ConsultarAlmacenTachoPort {

    /**
     * Consulta las partidas de almacén tacho según los criterios especificados
     * 
     * @param request Criterios de consulta (almacén, paginación, etc.)
     * @return Flux de partidas de almacén tacho
     */
    Mono<AlmacenTachoResponse> consultarAlmacenTacho(ConsultarAlmacenTachoRequest request);

}
