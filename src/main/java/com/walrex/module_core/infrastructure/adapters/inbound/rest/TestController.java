package com.walrex.module_core.infrastructure.adapters.inbound.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Controlador de prueba para el module-core
 */
@RestController
@Tag(name = "Test", description = "Endpoints de prueba del sistema")
@Slf4j
public class TestController {

    @GetMapping("/test")
    @Operation(summary = "Endpoint de prueba", description = "Verifica que el module-core esté funcionando correctamente")
    public Mono<String> test() {
        log.debug("🧪 Test endpoint solicitado");
        return Mono.just("Module Core is working!");
    }
}
