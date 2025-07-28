package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.walrex.module_ecomprobantes.application.ports.output.LycetServicePort;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionRequest;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador HTTP para consumir el servicio de Lycet.
 * Implementa el puerto LycetServicePort con manejo robusto de errores y logging
 * detallado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LycetServiceAdapter implements LycetServicePort {

    private final WebClient webClient;

    @Value("${lycet.api.base-url}")
    private String baseUrl;

    @Value("${lycet.api.endpoint-despatch}")
    private String endpoint;

    @Value("${lycet.api.timeout:30}")
    private int timeoutSeconds;

    @Value("${lycet.api.token}")
    private String token;

    @Override
    public Mono<LycetGuiaRemisionResponse> enviarGuiaRemision(LycetGuiaRemisionRequest request) {
        String correlativo = request.getCorrelativo();
        String serie = request.getSerie();

        log.info("🚀 Iniciando envío de guía de remisión a Lycet");
        log.info("📋 Detalles del envío:");
        log.info("   - Serie: {}", serie);
        log.info("   - Correlativo: {}", correlativo);
        log.info("   - Tipo Documento: {}", request.getTipoDoc());
        log.info("   - Fecha Emisión: {}", request.getFechaEmision());
        log.info("   - URL destino: {}{}", baseUrl, endpoint);
        log.info("   - Timeout: {} segundos", timeoutSeconds);

        return webClient.post()
                .uri(baseUrl + endpoint + "?token=" + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LycetGuiaRemisionResponse.class)
                .doOnNext(response -> {
                    log.info("✅ Respuesta exitosa de Lycet para serie: {} - correlativo: {}", serie, correlativo);
                    log.info("📊 Detalles de la respuesta:");
                    log.info("   - Success: {}", response.getSuccess());
                    log.info("   - Message: {}", response.getMessage());
                    log.info("   - Código SUNAT: {}", response.getSunatCode());
                    log.info("   - Descripción SUNAT: {}", response.getSunatDescription());
                    log.info("   - Número Comprobante: {}", response.getNumeroComprobante());
                    log.info("   - Hash: {}", response.getHash() != null ? "Presente" : "No disponible");
                    log.info("   - XML Firmado: {}", response.getXmlFirmado() != null ? "Presente" : "No disponible");
                    log.info("   - PDF: {}", response.getPdf() != null ? "Presente" : "No disponible");
                    log.info("   - CDR: {}", response.getCdr() != null ? "Presente" : "No disponible");

                    if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                        log.warn("⚠️ Errores en la respuesta de Lycet:");
                        response.getErrors().forEach(error -> log.warn("   - Código: {}, Campo: {}, Mensaje: {}",
                                error.getCode(), error.getField(), error.getMessage()));
                    }
                })
                .doOnError(error -> {
                    log.error("❌ Error en comunicación con Lycet para serie: {} - correlativo: {}", serie, correlativo);
                    log.error("🔍 Detalles del error:");
                    log.error("   - Tipo: {}", error.getClass().getSimpleName());
                    log.error("   - Mensaje: {}", error.getMessage());

                    if (error instanceof WebClientResponseException webClientError) {
                        log.error("   - Status Code: {}", webClientError.getStatusCode());
                        log.error("   - Status Text: {}", webClientError.getStatusText());
                        log.error("   - Response Body: {}", webClientError.getResponseBodyAsString());
                    }

                    if (error.getCause() != null) {
                        log.error("   - Causa: {}", error.getCause().getMessage());
                    }
                })
                .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .onErrorResume(throwable -> {
                    log.error("❌ Error inesperado en comunicación con Lycet: {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("Error enviando comprobante a Lycet", throwable));
                });
    }

    /**
     * Maneja errores específicos de WebClient con logging detallado.
     */
    private Mono<LycetGuiaRemisionResponse> handleWebClientError(WebClientResponseException error) {
        int statusCode = error.getStatusCode().value();
        String statusText = error.getStatusText();
        String responseBody = error.getResponseBodyAsString();

        log.error("🌐 Error HTTP en comunicación con Lycet:");
        log.error("   - Status: {} ({})", statusCode, statusText);
        log.error("   - Response Body: {}", responseBody);

        // Crear respuesta de error estructurada
        LycetGuiaRemisionResponse errorResponse = LycetGuiaRemisionResponse.builder()
                .success(false)
                .message("Error de comunicación con Lycet: " + statusText)
                .sunatCode("HTTP_" + statusCode)
                .sunatDescription("Error de comunicación HTTP")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return Mono.just(errorResponse);
    }
}