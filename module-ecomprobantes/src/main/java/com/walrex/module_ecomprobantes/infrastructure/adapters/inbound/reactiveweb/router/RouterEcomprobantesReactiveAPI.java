package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.reactiveweb.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.reactiveweb.GenerarPDFHandler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterEcomprobantesReactiveAPI {

        private final GenerarPDFHandler generarPDFHandler;
        private static final String PATH_ECOMPROBANTES = "ecomprobantes";

        @Bean
        public RouterFunction<ServerResponse> ecomprobantesRouter() {
                return RouterFunctions.route()
                                .path("/" + PATH_ECOMPROBANTES, () -> RouterFunctions.route()
                                                .GET("/guia-remision/pdf/{idComprobante}",
                                                                generarPDFHandler::generarPDFComprobante)
                                                .GET("/guia-remision/html/{idComprobante}",
                                                                generarPDFHandler::generarHTMLComprobante)
                                                .POST("/guia-remision/send-sunat/{idComprobante}",
                                                                generarPDFHandler::enviarGuiaRemisionLycet)
                                                .build())
                                .before(request -> {
                                        log.info("🔄 Router {} recibió solicitud: {} {}", PATH_ECOMPROBANTES,
                                                        request.method(), request.path());
                                        return request;
                                })
                                .after((request, response) -> {
                                        log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_ECOMPROBANTES,
                                                        request.method(),
                                                        request.path(), response.statusCode());
                                        return response;
                                })
                                .build();
        }

        @PostConstruct
        public void init() {
                log.info("🔌 Rutas del módulo de comprobantes registradas en: /{}", PATH_ECOMPROBANTES);
                log.info("📋 Rutas disponibles:");
                log.info("   - GET  /{}/guia-remision/pdf/{idComprobante} - Generar PDF de guía de remisión",
                                PATH_ECOMPROBANTES);
                log.info("   - GET  /{}/guia-remision/html/{idComprobante} - Generar HTML de guía de remisión",
                                PATH_ECOMPROBANTES);
                log.info("   - POST /{}/guia-remision/lycet/{idComprobante} - Enviar guía de remisión a Lycet",
                                PATH_ECOMPROBANTES);
        }
}