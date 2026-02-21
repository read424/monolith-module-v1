package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse;
import reactor.core.publisher.Mono;

public interface ObtenerSessionArticuloPesajeUseCase {
    Mono<SessionArticuloPesajeResponse> obtenerSession(Integer idDetOrdenIngreso);
}
