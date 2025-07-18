package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.ResponseEventGuiaDataDto;

import reactor.core.publisher.Mono;

public interface DevolucionServiciosPersistencePort {
    Mono<Void> actualizarIdComprobante(ResponseEventGuiaDataDto dataResponse, String comprobanteId);
}
