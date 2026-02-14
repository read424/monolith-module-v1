package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import reactor.core.publisher.Mono;

public interface PesajeUseCase {
    Mono<PesajeDetalle> registrarPesaje(PesajeRequest request);
}
