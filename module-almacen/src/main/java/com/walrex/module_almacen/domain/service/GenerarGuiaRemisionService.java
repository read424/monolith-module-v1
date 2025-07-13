package com.walrex.module_almacen.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.GenerarGuiaRemisionUseCase;
import com.walrex.module_almacen.application.ports.output.GuiaRemisionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerarGuiaRemisionService implements GenerarGuiaRemisionUseCase {

    private final GuiaRemisionPersistencePort guiaRemisionPersistencePort;

    @Override
    public Mono<GuiaRemisionGeneradaDTO> generarGuiaRemision(GuiaRemisionGeneradaDTO request) {
        log.info("🚚 Iniciando generación de guía de remisión para orden: {}", request.getIdOrdenSalida());

        return validarRequest(request)
                .then(guiaRemisionPersistencePort.validarOrdenSalidaParaGuia(request.getIdOrdenSalida()))
                .flatMap(esValida -> {
                    if (!esValida) {
                        return Mono.error(new IllegalArgumentException(
                                "La orden de salida " + request.getIdOrdenSalida()
                                        + " no es válida para generar guía"));
                    }
                    return guiaRemisionPersistencePort.generarGuiaRemision(request);
                })
                .doOnNext(resultado -> log.info(
                        "✅ Guía de remisión generada exitosamente para orden: {} - Fecha entrega: {}",
                        resultado.getIdOrdenSalida(), resultado.getFechaEntrega()))
                .doOnError(error -> log.error("❌ Error al generar guía de remisión para orden: {} - Error: {}",
                        request.getIdOrdenSalida(), error.getMessage()));
    }

    private Mono<Void> validarRequest(GuiaRemisionGeneradaDTO request) {
        if (request.getIdOrdenSalida() == null) {
            return Mono.error(new IllegalArgumentException("ID de orden de salida es requerido"));
        }
        if (request.getNumDocChofer() == null || request.getNumDocChofer().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Número de documento del chofer es requerido"));
        }
        if (request.getNumPlaca() == null || request.getNumPlaca().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Número de placa es requerido"));
        }

        log.debug("✅ Request de guía de remisión validado correctamente");
        return Mono.empty();
    }
}
