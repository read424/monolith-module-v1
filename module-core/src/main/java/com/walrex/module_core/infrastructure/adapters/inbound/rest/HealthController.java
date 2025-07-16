package com.walrex.module_core.infrastructure.adapters.inbound.rest;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Controlador de health check para el module-core
 * 
 * Proporciona endpoints de monitoreo y estado del sistema
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Endpoints para verificar el estado del sistema")
@Slf4j
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar estado del sistema", description = "Obtiene información sobre el estado actual del module-core")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sistema funcionando correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HealthResponse.class)))
    })
    public Mono<HealthResponse> health() {
        log.debug("🔍 Health check solicitado");

        return Mono.just(HealthResponse.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .module("module-core")
                .version("1.0.0")
                .build());
    }

    @GetMapping("/info")
    @Operation(summary = "Información del sistema", description = "Obtiene información detallada sobre el sistema")
    public Mono<Map<String, Object>> info() {
        return Mono.just(Map.of(
                "application", "module-core",
                "version", "1.0.0",
                "java.version", System.getProperty("java.version"),
                "spring.version", "3.3.11",
                "timestamp", LocalDateTime.now()));
    }

    /**
     * DTO para respuesta de health check
     */
    public static class HealthResponse {
        private String status;
        private LocalDateTime timestamp;
        private String module;
        private String version;

        // Constructor, getters, setters y builder
        public HealthResponse() {
        }

        public HealthResponse(String status, LocalDateTime timestamp, String module, String version) {
            this.status = status;
            this.timestamp = timestamp;
            this.module = module;
            this.version = version;
        }

        public static HealthResponseBuilder builder() {
            return new HealthResponseBuilder();
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public static class HealthResponseBuilder {
            private String status;
            private LocalDateTime timestamp;
            private String module;
            private String version;

            HealthResponseBuilder() {
            }

            public HealthResponseBuilder status(String status) {
                this.status = status;
                return this;
            }

            public HealthResponseBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public HealthResponseBuilder module(String module) {
                this.module = module;
                return this;
            }

            public HealthResponseBuilder version(String version) {
                this.version = version;
                return this;
            }

            public HealthResponse build() {
                return new HealthResponse(status, timestamp, module, version);
            }
        }
    }
}