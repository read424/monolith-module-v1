package com.walrex.module_almacen.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.ActualizarComprobanteDevolucionUseCase;
import com.walrex.module_almacen.application.ports.output.DevolucionServiciosPersistencePort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActualizarComprobanteDevolucionService implements ActualizarComprobanteDevolucionUseCase {
    private final DevolucionServiciosPersistencePort devolucionServiciosPersistencePort;

    @Override
    public Mono<Void> actualizarComprobanteDevolucion(
            GuiaRemisionResponseEventDTO responseDTO, String correlationId) {
        return devolucionServiciosPersistencePort.actualizarIdComprobante(responseDTO.getData(), correlationId)
                .doOnSuccess(unused -> log.debug("✅ Comprobante actualizado para guía de remisión: {}", correlationId))
                .doOnError(error -> log.error("❌ Error al actualizar comprobante: {}", error.getMessage()));
    }
}
