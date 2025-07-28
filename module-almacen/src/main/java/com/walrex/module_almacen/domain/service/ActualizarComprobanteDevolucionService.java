package com.walrex.module_almacen.domain.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.ActualizarComprobanteDevolucionUseCase;
import com.walrex.module_almacen.application.ports.output.DevolucionServiciosPersistencePort;
import com.walrex.module_almacen.application.ports.output.EventPublisherOutputPort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActualizarComprobanteDevolucionService implements ActualizarComprobanteDevolucionUseCase {
    private final DevolucionServiciosPersistencePort devolucionServiciosPersistencePort;
    private final EventPublisherOutputPort eventPublisherOutputPort;

    @Override
    @CacheEvict(value = "ordenSalidaDevolucion", allEntries = true)
    public Mono<Void> actualizarComprobanteDevolucion(
            GuiaRemisionResponseEventDTO responseDTO, String correlationId) {
        log.info(
                "🔄 Actualizando comprobante y invalidando cache específico - CorrelationId: {}, Success: {}, OrdenSalida: {}",
                correlationId, responseDTO.getSuccess(), responseDTO.getData().getIdOrdenSalida());

        return devolucionServiciosPersistencePort.actualizarIdComprobante(responseDTO.getData(), correlationId)
                .then(eventPublisherOutputPort.publishGuiaDevolucionEvent(responseDTO))
                .doOnSuccess(unused -> {
                    log.info("✅ Comprobante actualizado y cache invalidado para guía de remisión: {}", correlationId);
                    log.info(
                            "🗑️ Cache 'ordenSalidaDevolucion' invalidado para orden: {} - próxima consulta reconstruirá datos específicos",
                            responseDTO.getData().getIdOrdenSalida());
                })
                .doOnError(error -> log.error("❌ Error al actualizar comprobante: {}", error.getMessage()));
    }
}
