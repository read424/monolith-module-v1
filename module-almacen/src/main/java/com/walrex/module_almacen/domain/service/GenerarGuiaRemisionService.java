package com.walrex.module_almacen.domain.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.GenerarGuiaRemisionUseCase;
import com.walrex.module_almacen.application.ports.output.EnviarGuiaRemisionEventPort;
import com.walrex.module_almacen.application.ports.output.GuiaRemisionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDataDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerarGuiaRemisionService implements GenerarGuiaRemisionUseCase {

    private final GuiaRemisionPersistencePort guiaRemisionPersistencePort;
    private final EnviarGuiaRemisionEventPort enviarGuiaRemisionEventPort;

    @Override
    public Mono<GuiaRemisionGeneradaDTO> generarGuiaRemision(GuiaRemisionGeneradaDTO request) {
        log.info("Iniciando generación de guía de remisión - Orden: {}",
                request);

        return validarRequest(request)
                .then(validarOrdenSalida(request))
                .then(persistirGuia(request))
                .flatMap(resultado -> procesarYEnviarEvento(resultado))
                .doOnError(error -> log.error(
                        "Error al generar guía de remisión - Orden: {}, Error: {}",
                        request.getIdOrdenSalida(), error.getMessage()));
    }

    private Mono<Boolean> validarOrdenSalida(GuiaRemisionGeneradaDTO request) {
        return guiaRemisionPersistencePort.validarOrdenSalidaParaGuia(request)
                .flatMap(esValida -> {
                    if (!esValida) {
                        return Mono.error(new IllegalArgumentException(
                                "La orden de salida " + request.getIdOrdenSalida()
                                        + " no es válida para generar guía"));
                    }
                    return Mono.just(true);
                });
    }

    private Mono<GuiaRemisionGeneradaDTO> persistirGuia(GuiaRemisionGeneradaDTO request) {
        return guiaRemisionPersistencePort.generarGuiaRemision(request)
                .doOnNext(resultado -> log.info("Guía de remisión persistida - Orden: {}, Fecha: {}",
                        resultado.getIdOrdenSalida(), resultado.getFechaEntrega()));
    }

    private Mono<GuiaRemisionGeneradaDTO> procesarYEnviarEvento(GuiaRemisionGeneradaDTO resultado) {
        String correlationId = UUID.randomUUID().toString();
        return guiaRemisionPersistencePort.obtenerDatosGuiaGenerada(resultado.getIdOrdenSalida().longValue())
                .doOnNext(guiaData -> {
                    guiaData.setIdUsuario(resultado.getIdUsuario());
                    log.info("Datos completos obtenidos - Orden: {}, Guia: {}, Items: {}",
                            guiaData
                                    .getIdOrdenSalida(),
                            guiaData,
                            guiaData.getDetailItems() != null ? guiaData.getDetailItems().size() : 0);
                })
                .flatMap(guiaData -> enviarEventoKafka(guiaData, correlationId, resultado.getIsGuiaSunat())
                        .thenReturn(resultado));
    }

    private Mono<Void> enviarEventoKafka(GuiaRemisionGeneradaDataDTO resultado,
            String correlationId, Boolean isComprobanteSUNAT) {
        return enviarGuiaRemisionEventPort.enviarEventoGuiaRemision(resultado, correlationId,
                isComprobanteSUNAT)
                .doOnSuccess(v -> log.info("Evento Kafka enviado - Orden: {}, CorrelationId: {}",
                        resultado.getIdOrdenSalida(), correlationId))
                .onErrorResume(kafkaError -> {
                    log.error(
                            "Error al enviar evento Kafka (continuando flujo) - Orden: {}, Error: {}, CorrelationId: {}",
                            resultado.getIdOrdenSalida(), kafkaError.getMessage(), correlationId);
                    return Mono.empty();
                });
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
        return Mono.empty();
    }
}
