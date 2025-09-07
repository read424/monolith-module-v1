package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.dto.AlmacenTachoResponseDTO;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;

import reactor.core.publisher.Mono;

/**
 * Caso de uso para consultar almacén tacho
 * Implementa la lógica de aplicación para la consulta de partidas
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ConsultarAlmacenTachoUseCase {

    /**
     * Ejecuta la consulta de almacén tacho
     * 
     * @param request Criterios de consulta
     * @return Mono con la respuesta de almacén tacho
     */
    Mono<AlmacenTachoResponseDTO> listarPartidasInTacho(ConsultarAlmacenTachoRequest request);  
}
