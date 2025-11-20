package com.walrex.despacho.module_liquidaciones.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.despacho.module_liquidaciones.infrastructure.adapters.inbound.reactiveweb.handler.ReporteDespachoHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReporteDespachoReactiveAPI {

    private static final String PATH_DESPACHO = "despacho";

    private final ReporteDespachoHandler reporteDespachoHandler;

    @Bean
    public RouterFunction<ServerResponse> rptDespachoLiquidaciones() {
        return RouterFunctions.route()
            .path("/" + PATH_DESPACHO, builder -> builder
                .POST("/reporte/salidas-despacho", reporteDespachoHandler::generarReporteDespachoSalidas)
            )
            .before(request -> {
                log.info("🔄 Router {} recibió solicitud: {} {}",
                    PATH_DESPACHO,
                    request.method(),
                    request.path());
                return request;
            })
            .after((request, response) -> {
                log.info("✅ Router {} respondió a: {} {} con estado: {}",
                    PATH_DESPACHO,
                    request.method(),
                    request.path(),
                    response.statusCode());
                return response;
            })
            .build();
    }

    @PostConstruct
    public void init() {
        System.out.println("🔌 Rutas del módulo despacho registradas en: /" + PATH_DESPACHO);
        log.info("🔌 Rutas del módulo despacho registradas en: /{}", PATH_DESPACHO);
    }
}
