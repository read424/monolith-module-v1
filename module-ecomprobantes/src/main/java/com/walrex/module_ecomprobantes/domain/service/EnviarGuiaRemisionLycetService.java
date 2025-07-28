package com.walrex.module_ecomprobantes.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_ecomprobantes.application.ports.input.EnviarGuiaRemisionLycetUseCase;
import com.walrex.module_ecomprobantes.application.ports.output.*;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionCabeceraProjection;
import com.walrex.module_ecomprobantes.domain.model.dto.ReferralGuideDTO;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionRequest;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.GuiaRemisionProjectionMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.LycetMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para el envío de guías de remisión a Lycet.
 * Orquesta el flujo completo: obtener datos → transformar → enviar → procesar
 * respuesta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnviarGuiaRemisionLycetService implements EnviarGuiaRemisionLycetUseCase {

    private final GuiaRemisionDataPort guiaRemisionDataPort;
    private final LycetServicePort lycetServicePort;
    private final ActualizarComprobanteLycetPort actualizarComprobanteLycetPort;
    private final GuiaRemisionProjectionMapper guiaRemisionProjectionMapper;
    private final LycetMapper lycetMapper;

    @Override
    public Mono<LycetGuiaRemisionResponse> enviarGuiaRemisionLycet(Integer idComprobante) {
        log.info("🚀 ===== INICIANDO PROCESO DE ENVÍO DE GUÍA DE REMISIÓN A LYCET =====");
        log.info("📋 Parámetros de entrada:");
        log.info("   - ID Comprobante: {}", idComprobante);
        log.info("   - Timestamp inicio: {}", java.time.LocalDateTime.now());

        return validarIdComprobante(idComprobante)
                .then(obtenerProyeccionGuiaRemision(idComprobante))
                .flatMap(this::enriquecerDatos)
                .flatMap(this::transformarParaLycet)
                .flatMap(this::enviarALycet)
                .flatMap(response -> procesarRespuestaLycet(response, idComprobante))
                .doOnSuccess(response -> {
                    log.info("✅ ===== PROCESO COMPLETADO EXITOSAMENTE =====");
                    log.info("📊 Resumen del proceso:");
                    log.info("   - ID Comprobante: {}", idComprobante);
                    log.info("   - Éxito: {}", response.getSuccess());
                    log.info("   - Código SUNAT: {}", response.getSunatCode());
                    log.info("   - Mensaje: {}", response.getMessage());
                    log.info("   - Timestamp fin: {}", java.time.LocalDateTime.now());
                })
                .doOnError(error -> {
                    log.error("❌ ===== ERROR EN EL PROCESO =====");
                    log.error("🔍 Detalles del error:");
                    log.error("   - ID Comprobante: {}", idComprobante);
                    log.error("   - Tipo de error: {}", error.getClass().getSimpleName());
                    log.error("   - Mensaje: {}", error.getMessage());
                    log.error("   - Timestamp error: {}", java.time.LocalDateTime.now());

                    if (error.getCause() != null) {
                        log.error("   - Causa raíz: {}", error.getCause().getMessage());
                    }
                });
    }

    /**
     * Valida que el ID del comprobante sea válido.
     */
    private Mono<Void> validarIdComprobante(Integer idComprobante) {
        return Mono.fromRunnable(() -> {
            if (idComprobante == null || idComprobante <= 0) {
                throw new IllegalArgumentException("El ID del comprobante debe ser válido y mayor a 0");
            }
            log.debug("✅ Validación de ID de comprobante completada");
        });
    }

    /**
     * Obtiene la proyección de datos de la guía de remisión desde la base de datos.
     */
    private Mono<GuiaRemisionCabeceraProjection> obtenerProyeccionGuiaRemision(Integer idComprobante) {
        return guiaRemisionDataPort.obtenerProyeccionGuiaRemision(idComprobante)
                .doOnNext(data -> log.debug("📊 Proyección obtenida para comprobante: {}", idComprobante));
    }

    /**
     * Enriquece los datos con información adicional y validaciones de negocio.
     * Convierte la proyección a ReferralGuideDTO usando el mapper.
     */
    private Mono<ReferralGuideDTO> enriquecerDatos(GuiaRemisionCabeceraProjection projection) {
        return Mono.fromCallable(() -> {
            // Convertir proyección a ReferralGuideDTO usando el mapper
            ReferralGuideDTO referralGuideDTO = guiaRemisionProjectionMapper.toReferralGuideDTO(projection);
            referralGuideDTO.setIdVersion("2022");

            // Validar que todos los datos requeridos estén presentes
            validarDatosCompletos(referralGuideDTO);

            log.debug("🔧 Datos enriquecidos para envío a Lycet: {}", referralGuideDTO);
            return referralGuideDTO;
        });
    }

    /**
     * Transforma ReferralGuideDTO a LycetGuiaRemisionRequest usando el mapper.
     */
    private Mono<LycetGuiaRemisionRequest> transformarParaLycet(ReferralGuideDTO referralGuideDTO) {
        return Mono.fromCallable(() -> {
            LycetGuiaRemisionRequest lycetRequest = lycetMapper.toLycetRequest(referralGuideDTO);
            log.debug("🔄 Datos transformados para Lycet: {}", lycetRequest.getCorrelativo());
            return lycetRequest;
        });
    }

    /**
     * Envía la solicitud al servicio de Lycet.
     */
    private Mono<LycetGuiaRemisionResponse> enviarALycet(LycetGuiaRemisionRequest lycetRequest) {
        log.info("📤 Enviando solicitud a Lycet");
        log.info("📋 Detalles de la solicitud:");
        log.info("   - Serie: {}", lycetRequest.getSerie());
        log.info("   - Correlativo: {}", lycetRequest.getCorrelativo());
        log.info("   - Tipo Documento: {}", lycetRequest.getTipoDoc());
        log.info("   - Fecha Emisión: {}", lycetRequest.getFechaEmision());
        log.info("   - Cantidad de detalles: {}",
                lycetRequest.getDetails() != null ? lycetRequest.getDetails().size() : 0);

        return lycetServicePort.enviarGuiaRemision(lycetRequest)
                .doOnNext(response -> log.debug("📤 Respuesta de Lycet recibida para correlativo: {}",
                        lycetRequest.getCorrelativo()));
    }

    /**
     * Procesa la respuesta de Lycet y actualiza la información en la base de datos.
     * Maneja tres escenarios:
     * 1. Éxito (200): Actualiza BD con información de SUNAT
     * 2. Error de validación (400): Solo devuelve error al handler
     * 3. Error de SUNAT (500): Solo devuelve error al handler
     */
    private Mono<LycetGuiaRemisionResponse> procesarRespuestaLycet(LycetGuiaRemisionResponse response,
            Integer idComprobante) {
        log.info("🔍 Procesando respuesta de Lycet");
        log.info("📊 Análisis de la respuesta:");
        log.info("   - Éxito: {}", response.getSuccess());
        log.info("   - Código SUNAT: {}", response.getSunatCode());
        log.info("   - Descripción SUNAT: {}", response.getSunatDescription());

        // 1. ✅ Status 200 - Éxito: Usar adapter para actualizar BD
        if (response.getSuccess() && response.getSunatResponse() != null) {
            log.info("✅ Respuesta exitosa - Actualizando BD con información de SUNAT");
            return actualizarComprobanteLycetPort
                    .actualizarComprobanteConRespuestaLycet(idComprobante.longValue(), response)
                    .thenReturn(response)
                    .doOnSuccess(updatedResponse -> log.info("💾 Comprobante actualizado exitosamente en BD"));
        }

        // 2. ❌ Status 400 - Error de validación: Solo devolver error al handler
        if (esErrorDeValidacion(response)) {
            log.warn("⚠️ Error de validación (400) - NO se actualiza BD");
            log.warn("📝 Mensaje de validación: {}", response.getMessage());
            if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                response.getErrors().forEach(
                        error -> log.warn("   - Campo: {}, Mensaje: {}", error.getField(), error.getMessage()));
            }
            return Mono.just(response); // Sin actualizar BD
        }

        // 3. ❌ Status 500 - Error de SUNAT: Solo devolver error al handler
        log.warn("⚠️ Error de SUNAT (500) - NO se actualiza BD");
        log.warn("📝 Mensaje de error: {}", response.getMessage());
        if (response.getErrors() != null) {
            response.getErrors().forEach(error -> log.warn("   - Error: {} - Campo: {} - Mensaje: {}",
                    error.getCode(), error.getField(), error.getMessage()));
        }
        return Mono.just(response); // Sin actualizar BD
    }

    /**
     * Determina si la respuesta de Lycet es un error de validación (HTTP 400).
     * 
     * @param response Respuesta de Lycet
     * @return true si es un error de validación, false en caso contrario
     */
    private boolean esErrorDeValidacion(LycetGuiaRemisionResponse response) {
        // Un error de validación típicamente tiene:
        // - success = false o null
        // - errors array con detalles de validación
        // - sunatResponse = null (no es una respuesta de SUNAT)

        boolean tieneErroresDeValidacion = response.getErrors() != null && !response.getErrors().isEmpty();
        boolean noEsRespuestaSunAT = response.getSunatResponse() == null;
        boolean noEsExitoso = !Boolean.TRUE.equals(response.getSuccess());

        return tieneErroresDeValidacion && noEsRespuestaSunAT && noEsExitoso;
    }

    /**
     * Valida que todos los datos necesarios estén completos.
     */
    private void validarDatosCompletos(ReferralGuideDTO data) {
        if (data.getCompany() == null) {
            throw new IllegalStateException("Los datos de la empresa son obligatorios para envío a Lycet");
        }
        if (data.getReceiver() == null) {
            throw new IllegalStateException("Los datos del destinatario son obligatorios para envío a Lycet");
        }
        if (data.getShipment() == null) {
            throw new IllegalStateException("Los datos del envío son obligatorios para envío a Lycet");
        }
        if (data.getDetalle() == null || data.getDetalle().isEmpty()) {
            throw new IllegalStateException("Los detalles del envío son obligatorios para envío a Lycet");
        }
    }
}