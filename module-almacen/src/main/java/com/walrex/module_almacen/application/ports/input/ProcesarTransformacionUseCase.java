package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.TransformacionResponseDTO;
import reactor.core.publisher.Mono;

public interface ProcesarTransformacionUseCase {
    Mono<TransformacionResponseDTO> procesarTransformacion(OrdenIngresoTransformacionDTO request);
}
