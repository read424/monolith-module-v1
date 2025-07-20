package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;

import reactor.core.publisher.Mono;

public interface AprobarSalidaInsumosUseCase {
    Mono<OrdenEgresoDTO> aprobarSalidaInsumos(AprobarSalidaRequerimiento request);
}
