package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.reactiveweb;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_ecomprobantes.application.ports.input.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerarPDFHandler {

        private final GenerarPDFGuiaRemisionUseCase generarPDFGuiaRemisionUseCase;
        private final GenerarHTMLGuiaRemisionUseCase generarHTMLGuiaRemisionUseCase;
        private final EnviarGuiaRemisionLycetUseCase enviarGuiaRemisionLycetUseCase;

        public Mono<ServerResponse> generarHTMLComprobante(ServerRequest request) {
                String idComprobante = request.pathVariable("idComprobante");

                log.info("📥 GET /ecomprobantes/guia-remision/html/{idComprobante}", idComprobante);

                return generarHTMLGuiaRemisionUseCase.generarHTMLGuiaRemision(Integer.parseInt(idComprobante))
                                .flatMap(html -> {
                                        log.info("✅ HTML generado exitosamente para comprobante: {} - {} caracteres",
                                                        idComprobante, html.length());
                                        return ServerResponse.ok()
                                                        .contentType(MediaType.TEXT_HTML)
                                                        .header("Content-Disposition",
                                                                        "inline; filename=\"guia-remision-"
                                                                                        + idComprobante + ".html\"")
                                                        .bodyValue(html);
                                })
                                .onErrorResume(IllegalArgumentException.class, ex -> {
                                        log.warn("⚠️ Error de validación: {}", ex.getMessage());
                                        return ServerResponse.badRequest().bodyValue(Map.of(
                                                        "error", "Datos inválidos",
                                                        "message", ex.getMessage()));
                                })
                                .onErrorResume(ex -> {
                                        log.error("❌ Error al generar HTML: ", ex);
                                        return ServerResponse.status(500).bodyValue(Map.of(
                                                        "error", "Error interno del servidor",
                                                        "message", "No se pudo generar el HTML"));
                                });
        }

        public Mono<ServerResponse> generarPDFComprobante(ServerRequest request) {
                String idComprobante = request.pathVariable("idComprobante");

                log.info("📥 GET /ecomprobantes/guia-remision/pdf/{idComprobante}", idComprobante);

                return generarPDFGuiaRemisionUseCase.generarPDFGuiaRemision(Integer.parseInt(idComprobante))
                                .flatMap(pdfBytes -> {
                                        log.info("✅ PDF generado exitosamente para comprobante: {} - {} bytes",
                                                        idComprobante, pdfBytes.length);
                                        return ServerResponse.ok()
                                                        .contentType(MediaType.APPLICATION_PDF)
                                                        .header("Content-Disposition",
                                                                        "attachment; filename=\"guia-remision-"
                                                                                        + idComprobante + ".pdf\"")
                                                        .bodyValue(pdfBytes);
                                })
                                .onErrorResume(IllegalArgumentException.class, ex -> {
                                        log.warn("⚠️ Error de validación: {}", ex.getMessage());
                                        return ServerResponse.badRequest().bodyValue(Map.of(
                                                        "error", "Datos inválidos",
                                                        "message", ex.getMessage()));
                                })
                                .onErrorResume(ex -> {
                                        log.error("❌ Error al generar PDF: ", ex);
                                        return ServerResponse.status(500).bodyValue(Map.of(
                                                        "error", "Error interno del servidor",
                                                        "message", "No se pudo generar el PDF"));
                                });
        }

        public Mono<ServerResponse> enviarGuiaRemisionLycet(ServerRequest request) {
                String idComprobante = request.pathVariable("idComprobante");

                log.info("📥 POST /ecomprobantes/guia-remision/lycet/{idComprobante}", idComprobante);

                return enviarGuiaRemisionLycetUseCase.enviarGuiaRemisionLycet(Integer.parseInt(idComprobante))
                                .flatMap(response -> {
                                        log.info("✅ Guía de remisión procesada exitosamente para comprobante: {}",
                                                        idComprobante);

                                        // Construir respuesta estructurada
                                        Map<String, Object> responseBody = Map.of(
                                                        "success", response.getSuccess(),
                                                        "message", response.getMessage(),
                                                        "comprobante", idComprobante,
                                                        "sunatCode", response.getSunatCode(),
                                                        "sunatDescription", response.getSunatDescription(),
                                                        "numeroComprobante", response.getNumeroComprobante(),
                                                        "timestamp", response.getTimestamp(),
                                                        "hasXml", response.getXmlFirmado() != null,
                                                        "hasPdf", response.getPdf() != null,
                                                        "hasCdr", response.getCdr() != null);

                                        return ServerResponse.ok()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(responseBody);
                                })
                                .onErrorResume(IllegalArgumentException.class, ex -> {
                                        log.warn("⚠️ Error de validación: {}", ex.getMessage());
                                        return ServerResponse.badRequest().bodyValue(Map.of(
                                                        "error", "Datos inválidos",
                                                        "message", ex.getMessage(),
                                                        "comprobante", idComprobante));
                                })
                                .onErrorResume(Exception.class, ex -> {
                                        log.error("❌ Error al enviar a Lycet: ", ex);
                                        return ServerResponse.status(500).bodyValue(Map.of(
                                                        "error", "Error interno del servidor",
                                                        "message", "No se pudo enviar la guía de remisión a Lycet",
                                                        "comprobante", idComprobante,
                                                        "details", ex.getMessage()));
                                });
        }
}