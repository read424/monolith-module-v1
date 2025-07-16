package com.walrex.module_ecomprobantes.domain.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.walrex.avro.schemas.*;
import com.walrex.module_ecomprobantes.application.ports.input.ProcesarGuiaRemisionUseCase;
import com.walrex.module_ecomprobantes.application.ports.output.ComprobantePersistencePort;
import com.walrex.module_ecomprobantes.application.ports.output.EnviarRespuestaGuiaRemisionPort;
import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.GuiaRemisionComprobanteMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarGuiaRemisionService implements ProcesarGuiaRemisionUseCase {

        private final ComprobantePersistencePort comprobantePersistencePort;
        private final EnviarRespuestaGuiaRemisionPort enviarRespuestaPort;
        private final GuiaRemisionComprobanteMapper guiaRemisionComprobanteMapper;

        @Override
        public Mono<Void> procesarGuiaRemision(CreateGuiaRemisionRemitenteMessage message, String correlationId) {
                log.info("🔄 Iniciando procesamiento de guía de remisión - Cliente: {}, Items: {}, CorrelationId: {}",
                                message.getIdCliente(), message.getDetailItems().size(), correlationId);

                return validarMensaje(message)
                                .flatMap(validMessage -> crearComprobanteCompleto(validMessage, correlationId))
                                .flatMap(comprobante -> enviarRespuestaExitosa(comprobante, correlationId))
                                .onErrorResume(error -> manejarError(error, correlationId))
                                .then()
                                .doOnSuccess(
                                                v -> log.info("✅ Guía de remisión procesada completamente - CorrelationId: {}",
                                                                correlationId))
                                .doOnError(
                                                error -> log.error(
                                                                "❌ Error final procesando guía de remisión - CorrelationId: {}, Error: {}",
                                                                correlationId, error.getMessage()));
        }

        private Mono<CreateGuiaRemisionRemitenteMessage> validarMensaje(CreateGuiaRemisionRemitenteMessage message) {
                return Mono.fromCallable(() -> {
                        // Validaciones básicas
                        if (message.getIdCliente() <= 0) {
                                throw new IllegalArgumentException("ID Cliente inválido: " + message.getIdCliente());
                        }

                        if (message.getDetailItems() == null || message.getDetailItems().isEmpty()) {
                                throw new IllegalArgumentException("Lista de items vacía");
                        }

                        if (message.getIdMotivo() <= 0) {
                                throw new IllegalArgumentException("ID Motivo inválido: " + message.getIdMotivo());
                        }

                        log.debug("✅ Mensaje validado exitosamente - Cliente: {}", message.getIdCliente());
                        return message;
                });
        }

        private Mono<ComprobanteDTO> crearComprobanteCompleto(
                        CreateGuiaRemisionRemitenteMessage message, String correlationId) {
                log.debug("📝 Creando comprobante completo para cliente: {} con {} items - CorrelationId: {}",
                                message.getIdCliente(), message.getDetailItems().size(), correlationId);

                // ✅ Mapear mensaje a ComprobanteDTO con detalles automáticos
                ComprobanteDTO comprobante = guiaRemisionComprobanteMapper.toComprobanteDTO(message);

                log.debug("📋 Comprobante preparado - Cliente: {}, Detalles: {}, Subtotal: {}",
                                comprobante.getIdCliente(), comprobante.getDetalles().size(),
                                comprobante.getSubtotal());

                // ✅ Crear comprobante en base de datos (operación reactiva)
                return comprobantePersistencePort.crearComprobante(comprobante)
                                .doOnNext(comprobanteCreado -> log.info(
                                                "✅ Comprobante completo creado - ID: {}, Cliente: {}, Items: {}, CorrelationId: {}",
                                                comprobanteCreado.getIdComprobante(), comprobanteCreado.getIdCliente(),
                                                comprobanteCreado.getDetalles().size(), correlationId))
                                .doOnError(error -> log.error(
                                                "❌ Error creando comprobante completo - CorrelationId: {}, Error: {}",
                                                correlationId, error.getMessage()));
        }

        private Mono<Void> enviarRespuestaExitosa(ComprobanteDTO comprobante, String correlationId) {
                log.debug("📤 Enviando respuesta exitosa - Comprobante: {}, Items: {}, CorrelationId: {}",
                                comprobante.getIdComprobante(), comprobante.getDetalles(), correlationId);

                return Mono.fromCallable(() -> {
                        // Crear datos de respuesta
                        GuiaRemisionRemitenteData responseData = GuiaRemisionRemitenteData.newBuilder()
                                        .setIdOrdensalida(extractIdOrdenSalidaFromObservacion(
                                                        comprobante.getObservacion()))
                                        .setIdComprobante(comprobante.getIdComprobante().intValue())
                                        .setCodigoComprobante(generateCodigoComprobante(comprobante))
                                        .build();

                        // Crear respuesta
                        GuiaRemisionRemitenteResponse response = GuiaRemisionRemitenteResponse.newBuilder()
                                        .setSuccess(true)
                                        .setMessage(String.format(
                                                        "Comprobante de guía de remisión creado exitosamente con %d items",
                                                        comprobante.getDetalles()))
                                        .setData(Arrays.asList(responseData))
                                        .build();

                        return response;
                })
                                .flatMap(response -> enviarRespuestaPort.enviarRespuesta(response, correlationId))
                                .doOnNext(v -> log.info("✅ Respuesta enviada exitosamente - CorrelationId: {}",
                                                correlationId));
        }

        private Mono<Void> manejarError(Throwable error, String correlationId) {
                log.error("❌ Error procesando guía de remisión - CorrelationId: {}, Error: {}", correlationId,
                                error.getMessage());

                return Mono.fromCallable(() -> {
                        // Crear respuesta de error
                        GuiaRemisionRemitenteResponse response = GuiaRemisionRemitenteResponse.newBuilder()
                                        .setSuccess(false)
                                        .setMessage("Error procesando guía de remisión: " + error.getMessage())
                                        .setData(Arrays.asList()) // Lista vacía
                                        .build();

                        return response;
                })
                                .flatMap(response -> enviarRespuestaPort.enviarRespuesta(response, correlationId))
                                .doOnNext(v -> log.info("✅ Respuesta de error enviada - CorrelationId: {}",
                                                correlationId))
                                .onErrorResume(sendError -> {
                                        log.error("❌ Error adicional enviando respuesta de error - CorrelationId: {}, Error: {}",
                                                        correlationId, sendError.getMessage());
                                        return Mono.empty();
                                });
        }

        private Integer extractIdOrdenSalidaFromObservacion(String observacion) {
                // Implementar lógica para extraer ID de orden de salida de la observación
                // Por ahora retornamos un valor por defecto
                return 1000; // TODO: Implementar extracción real
        }

        private String generateCodigoComprobante(ComprobanteDTO comprobante) {
                // Generar código del comprobante basado en serie y número
                return String.format("GR-%03d-%08d",
                                comprobante.getTipoSerie() != null ? comprobante.getTipoSerie() : 1,
                                comprobante.getIdComprobante());
        }
}
