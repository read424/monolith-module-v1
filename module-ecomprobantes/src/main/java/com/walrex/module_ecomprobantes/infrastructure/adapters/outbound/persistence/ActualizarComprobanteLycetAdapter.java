package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.ActualizarComprobanteLycetPort;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository.ComprobantesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador para actualizar la información del comprobante
 * después de recibir la respuesta de Lycet.
 * 
 * Implementa el puerto ActualizarComprobanteLycetPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActualizarComprobanteLycetAdapter implements ActualizarComprobanteLycetPort {

    private final ComprobantesRepository comprobantesRepository;

    @Override
    public Mono<Void> actualizarComprobanteConRespuestaLycet(Long idComprobante, LycetGuiaRemisionResponse response) {
        log.info("🔄 Iniciando actualización de comprobante con respuesta de Lycet");
        log.info("📋 Parámetros de actualización:");
        log.info("   - ID Comprobante: {}", idComprobante);
        log.info("   - Success: {}", response.getSuccess());
        log.info("   - SunatCode: {}", response.getSunatCode());

        return procesarRespuestaYActualizar(idComprobante, response)
                .doOnSuccess(result -> {
                    log.info("✅ Comprobante actualizado exitosamente");
                    log.info("📊 Resumen de la actualización:");
                    log.info("   - ID Comprobante: {}", idComprobante);
                    log.info("   - Filas afectadas: {}", result);
                })
                .doOnError(error -> {
                    log.error("❌ Error al actualizar comprobante con respuesta de Lycet");
                    log.error("   - ID Comprobante: {}", idComprobante);
                    log.error("   - Error: {}", error.getMessage());
                })
                .then();
    }

    private Mono<Integer> procesarRespuestaYActualizar(Long idComprobante, LycetGuiaRemisionResponse response) {
        return Mono.fromCallable(() -> {
            // Determinar el status basado en el éxito de la operación
            // Si success es true, status = 2 (exitoso), si es false, status = 3 (error)
            Integer status = Boolean.TRUE.equals(response.getSuccess()) ? 2 : 3;

            // Determinar el código de respuesta SUNAT
            Integer codigoResponseSunat = null;
            String responseSunat = null;
            String numeroTicket = null;

            // Procesar la respuesta específica de SUNAT si está disponible
            if (response.getSunatResponse() != null) {
                LycetGuiaRemisionResponse.SunatResponse sunatResp = response.getSunatResponse();

                // Obtener el ticket desde sunatResponse
                if (sunatResp.getTicket() != null) {
                    numeroTicket = sunatResp.getTicket();
                }

                // Procesar errores de SUNAT si los hay
                if (sunatResp.getError() != null) {
                    LycetGuiaRemisionResponse.SunatError error = sunatResp.getError();
                    if (error.getCode() != null) {
                        try {
                            codigoResponseSunat = Integer.parseInt(error.getCode());
                        } catch (NumberFormatException e) {
                            log.warn("⚠️ No se pudo parsear el código de error SUNAT: {}", error.getCode());
                        }
                    }
                    responseSunat = error.getMessage();
                }
            }

            // Si no hay información en sunatResponse, usar los campos generales
            if (codigoResponseSunat == null && response.getErrors() != null && !response.getErrors().isEmpty()) {
                // Tomar el primer error como referencia
                LycetGuiaRemisionResponse.LycetError error = response.getErrors().get(0);
                if (error.getCode() != null) {
                    try {
                        codigoResponseSunat = Integer.parseInt(error.getCode());
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ No se pudo parsear el código de error: {}", error.getCode());
                    }
                }
                if (responseSunat == null) {
                    responseSunat = error.getMessage();
                }
            } else if (codigoResponseSunat == null && response.getSunatCode() != null) {
                // Si no hay errores específicos, usar el código SUNAT general
                try {
                    // Si el código es "200" o "0", considerarlo como exitoso
                    if ("200".equals(response.getSunatCode()) || "0".equals(response.getSunatCode())) {
                        codigoResponseSunat = 0; // Código de éxito
                    } else {
                        codigoResponseSunat = Integer.parseInt(response.getSunatCode());
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ No se pudo parsear el código SUNAT: {}", response.getSunatCode());
                }
            }

            // Determinar el mensaje de respuesta si no se ha establecido
            if (responseSunat == null) {
                responseSunat = response.getMessage();
                if (response.getSunatDescription() != null && !response.getSunatDescription().isEmpty()) {
                    responseSunat = response.getSunatDescription();
                }
            }

            // Obtener el número de ticket desde comprobanteInfo si no se obtuvo de
            // sunatResponse
            if (numeroTicket == null && response.getComprobanteInfo() != null
                    && response.getComprobanteInfo().getNumeroTicket() != null) {
                numeroTicket = response.getComprobanteInfo().getNumeroTicket();
            }

            // Si no hay ticket en comprobanteInfo, intentar obtenerlo de otros campos
            if (numeroTicket == null && response.getNumeroComprobante() != null) {
                // En algunos casos, el número de comprobante puede servir como ticket
                numeroTicket = response.getNumeroComprobante();
            }

            log.debug("🔧 Valores calculados para actualización:");
            log.debug("   - Status: {}", status);
            log.debug("   - Código Response SUNAT: {}", codigoResponseSunat);
            log.debug("   - Response SUNAT: {}", responseSunat);
            log.debug("   - Número Ticket: {}", numeroTicket);

            return new ActualizacionComprobanteData(status, codigoResponseSunat, responseSunat, numeroTicket);
        })
                .flatMap(data -> comprobantesRepository.actualizarComprobanteConRespuestaLycet(
                        idComprobante,
                        data.status,
                        data.codigoResponseSunat,
                        data.responseSunat,
                        data.numeroTicket));
    }

    /**
     * Clase interna para encapsular los datos de actualización.
     */
    private static class ActualizacionComprobanteData {
        final Integer status;
        final Integer codigoResponseSunat;
        final String responseSunat;
        final String numeroTicket;

        ActualizacionComprobanteData(Integer status, Integer codigoResponseSunat, String responseSunat,
                String numeroTicket) {
            this.status = status;
            this.codigoResponseSunat = codigoResponseSunat;
            this.responseSunat = responseSunat;
            this.numeroTicket = numeroTicket;
        }
    }
}