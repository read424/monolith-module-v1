package com.walrex.module_ecomprobantes.domain.service;

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
                log.info("🔄 Iniciando procesamiento de guía de remisión - Guia: {}, Cliente: {}, Items: {}, CorrelationId: {}",
                                message, message.getIdCliente(), message.getDetailItems().size(), correlationId);

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

                log.debug("📋 Comprobante preparado - Comprobante: {}, Detalles: {}, Subtotal: {}",
                                comprobante, comprobante.getDetalles().size(),
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
                                        .setIdComprobante(comprobante.getIdComprobante().intValue())
                                        .setCodigoComprobante(comprobante.getCodigoComprobante())
                                        .setIdOrdensalida(comprobante.getIdOrdenSalida())
                                        .build();

                        log.debug("📋 ResponseData creado - IdComprobante: {}, CodigoComprobante: {}, IdOrdenSalida: {}",
                                        responseData.getIdComprobante(), responseData.getCodigoComprobante(),
                                        responseData.getIdOrdensalida());

                        // Crear respuesta
                        GuiaRemisionRemitenteResponse response = GuiaRemisionRemitenteResponse.newBuilder()
                                        .setSuccess(true)
                                        .setMessage(String.format(
                                                        "Comprobante de guía de remisión creado exitosamente con %d items",
                                                        comprobante.getDetalles().size()))
                                        .setData(responseData)
                                        .build();

                        // Log detallado de la respuesta completa
                        log.info("📤 Respuesta completa creada - Success: {}, Message: {}, Data: {}",
                                        response.getSuccess(),
                                        response.getMessage(),
                                        String.format("IdComprobante=%d, CodigoComprobante=%s, IdOrdenSalida=%d",
                                                        response.getData().getIdComprobante(),
                                                        response.getData().getCodigoComprobante(),
                                                        response.getData().getIdOrdensalida()));

                        return response;
                })
                                .doOnNext(response -> log.info(
                                                "📤 [ECOMPROBANTES] Enviando respuesta Kafka - CorrelationId: {}, Response: {}",
                                                correlationId, response))
                                .flatMap(response -> enviarRespuestaPort.enviarRespuesta(response, correlationId))
                                .doOnNext(v -> log.info("✅ Respuesta enviada exitosamente - CorrelationId: {}",
                                                correlationId))
                                .doOnError(error -> log.error(
                                                "❌ Error al enviar respuesta exitosa - CorrelationId: {}, Error: {}",
                                                correlationId, error.getMessage(), error))
                                .onErrorResume(error -> {
                                        log.error("🔄 Recuperando de error en envío de respuesta - CorrelationId: {}, Error: {}",
                                                        correlationId, error.getMessage());
                                        // Aquí puedes implementar lógica de retry o fallback
                                        return Mono.empty();
                                });
        }

        private Mono<Void> manejarError(Throwable error, String correlationId) {
                log.error("❌ Error procesando guía de remisión - CorrelationId: {}, Error: {}", correlationId,
                                error.getMessage());

                GuiaRemisionRemitenteResponse response = new GuiaRemisionRemitenteResponse();
                response.setSuccess(false);
                response.setMessage(error.getMessage());
                // Puedes setear otros campos si lo necesitas

                return enviarRespuestaError(response, correlationId);
        }

        private Mono<Void> enviarRespuestaError(GuiaRemisionRemitenteResponse response, String correlationId) {
                return enviarRespuestaPort
                                .enviarRespuesta(response, correlationId)
                                .doOnSuccess(v -> log.info(
                                                "🚨 Respuesta de error enviada por Kafka - CorrelationId: {}",
                                                correlationId))
                                .doOnError(e -> log.error(
                                                "❌ Error enviando respuesta de error por Kafka - CorrelationId: {}, Error: {}",
                                                correlationId, e.getMessage()))
                                .then();
        }
}
