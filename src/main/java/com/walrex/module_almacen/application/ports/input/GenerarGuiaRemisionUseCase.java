package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;

import reactor.core.publisher.Mono;

public interface GenerarGuiaRemisionUseCase {

    Mono<GuiaRemisionGeneradaDTO> generarGuiaRemision(GuiaRemisionGeneradaDTO request);
}
