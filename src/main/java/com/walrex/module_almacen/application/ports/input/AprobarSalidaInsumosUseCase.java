package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.ResponseAprobacionRequerimientoDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaRequestDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaResponseDTO;
import reactor.core.publisher.Mono;

public interface AprobarSalidaInsumosUseCase {
    Mono<ResponseAprobacionRequerimientoDTO> aprobarSalidaInsumos(AprobarSalidaRequerimiento request);
}
