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
        log.info("[INICIO] generarGuiaRemision - Request recibido: {}", request);
        return validarRequest(request)
                .then(validarOrdenSalida(request))
                .then(persistirGuia(request))
                .flatMap(resultado -> procesarYEnviarEvento(resultado))
                .doOnError(error -> log.error(
                        "[ERROR] generarGuiaRemision - Orden: {}, Error: {}",
                        request.getIdOrdenSalida(), error.getMessage()));
    }

    private Mono<Boolean> validarOrdenSalida(GuiaRemisionGeneradaDTO request) {
        log.info("[INICIO] validarOrdenSalida - Orden: {}", request.getIdOrdenSalida());
        return guiaRemisionPersistencePort.validarOrdenSalidaParaGuia(request)
                .flatMap(esValida -> {
                    log.info("[RESULTADO] validarOrdenSalida - Orden: {}, esValida: {}", request.getIdOrdenSalida(),
                            esValida);
                    if (!esValida) {
                        log.error("[ERROR] validarOrdenSalida - La orden de salida {} no es válida para generar guía",
                                request.getIdOrdenSalida());
                        return Mono.error(new IllegalArgumentException(
                                "La orden de salida " + request.getIdOrdenSalida()
                                        + " no es válida para generar guía"));
                    }
                    return Mono.just(true);
                });
    }

    private Mono<GuiaRemisionGeneradaDTO> persistirGuia(GuiaRemisionGeneradaDTO request) {
        log.info("[INICIO] persistirGuia - Orden: {}", request.getIdOrdenSalida());
        return guiaRemisionPersistencePort.generarGuiaRemision(request)
                .doOnNext(resultado -> log.info(
                        "[EXITO] persistirGuia - Guía de remisión persistida - Orden: {}, Fecha: {}",
                        resultado.getIdOrdenSalida(), resultado.getFechaEntrega()))
                .doOnError(error -> log.error("[ERROR] persistirGuia - Orden: {}, Error: {}",
                        request.getIdOrdenSalida(), error.getMessage()));
    }

    private Mono<GuiaRemisionGeneradaDTO> procesarYEnviarEvento(GuiaRemisionGeneradaDTO resultado) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[INICIO] procesarYEnviarEvento - Orden: {}, CorrelationId: {}", resultado.getIdOrdenSalida(),
                correlationId);
        return guiaRemisionPersistencePort.obtenerDatosGuiaGenerada(resultado.getIdOrdenSalida().longValue())
                .doOnNext(guiaData -> {
                    guiaData.setIdUsuario(resultado.getIdUsuario());
                    log.info(
                            "[DATOS] procesarYEnviarEvento - Datos completos obtenidos - Orden: {}, Guia: {}, Items: {}",
                            guiaData.getIdOrdenSalida(),
                            guiaData,
                            guiaData.getDetailItems() != null ? guiaData.getDetailItems().size() : 0);
                })
                .flatMap(guiaData -> {
                    log.info("[EVENTO] procesarYEnviarEvento - Enviando evento Kafka - Orden: {}, CorrelationId: {}",
                            guiaData.getIdOrdenSalida(), correlationId);
                    return enviarEventoKafka(guiaData, correlationId, resultado.getIsGuiaSunat())
                            .thenReturn(resultado);
                })
                .doOnError(error -> log.error("[ERROR] procesarYEnviarEvento - Orden: {}, Error: {}",
                        resultado.getIdOrdenSalida(), error.getMessage()));
    }

    private Mono<Void> enviarEventoKafka(GuiaRemisionGeneradaDataDTO resultado,
            String correlationId, Boolean isComprobanteSUNAT) {
        log.info("[INICIO] enviarEventoKafka - Orden: {}, CorrelationId: {}, isComprobanteSUNAT: {}",
                resultado.getIdOrdenSalida(), correlationId, isComprobanteSUNAT);
        return enviarGuiaRemisionEventPort.enviarEventoGuiaRemision(resultado, correlationId,
                isComprobanteSUNAT)
                .doOnSuccess(
                        v -> log.info("[EXITO] enviarEventoKafka - Evento Kafka enviado - Orden: {}, CorrelationId: {}",
                                resultado.getIdOrdenSalida(), correlationId))
                .doOnError(kafkaError -> log.error(
                        "[ERROR] enviarEventoKafka - Error al enviar evento Kafka - Orden: {}, Error: {}, CorrelationId: {}",
                        resultado.getIdOrdenSalida(), kafkaError.getMessage(), correlationId))
                .onErrorResume(kafkaError -> {
                    log.error(
                            "[ERROR] enviarEventoKafka (continuando flujo) - Orden: {}, Error: {}, CorrelationId: {}",
                            resultado.getIdOrdenSalida(), kafkaError.getMessage(), correlationId);
                    return Mono.empty();
                });
    }

    private Mono<Void> validarRequest(GuiaRemisionGeneradaDTO request) {
        log.info("[INICIO] validarRequest - Request: {}", request);
        if (request.getIdOrdenSalida() == null) {
            log.error("[ERROR] validarRequest - ID de orden de salida es requerido. Request: {}", request);
            return Mono.error(new IllegalArgumentException("ID de orden de salida es requerido"));
        }
        if (request.getIdConductor() == null || request.getIdConductor() == 0) {
            log.error("[ERROR] validarRequest - ID del chofer es requerido. Request: {}", request);
            return Mono.error(new IllegalArgumentException("ID del chofer es requerido"));
        }
        if (request.getNumPlaca() == null || request.getNumPlaca().trim().isEmpty()) {
            log.error("[ERROR] validarRequest - Número de placa es requerido. Request: {}", request);
            return Mono.error(new IllegalArgumentException("Número de placa es requerido"));
        }
        log.info("[EXITO] validarRequest - Validación exitosa. Request: {}", request);
        return Mono.empty();
    }
}
