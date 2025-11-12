package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb;

import java.util.Map;
import java.util.stream.Collectors;

import com.walrex.module_comercial.application.ports.input.GuardarSolicitudCambioServicioUseCase;
import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.mapper.SolicitudCambioRequestMapper;
import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request.GuardarSolicitudCambioRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para endpoints de solicitud de cambio de servicio
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudCambioHandler {

    private final GuardarSolicitudCambioServicioUseCase guardarSolicitudCambioUseCase;
    private final UserContextProvider userContextProvider;
    private final SolicitudCambioRequestMapper requestMapper;
    private final Validator validator;

    /**
     * Handler para guardar solicitud de cambio de servicio
     * POST /comercial/guardar-solicitud-cambio-servicio
     */
    public Mono<ServerResponse> guardarSolicitudCambioServicio(ServerRequest request) {
        log.info("POST /comercial/guardar-solicitud-cambio-servicio - Iniciando handler");

        try {
            // Extraer información del usuario desde los headers usando module-security-commons
            JwtUserInfo userInfo = userContextProvider.getCurrentUser(request);
            String userId = userInfo.getUserId();
            log.info("👤 Usuario autenticado: {}", userId);

            return request.bodyToMono(GuardarSolicitudCambioRequest.class)
                    .doOnNext(req -> log.info("📥 Request body recibido - idPartida: {}, aplicarOtrasPartidas: {}",
                            req.getIdPartida(), req.getAplicarOtrasPartidas()))
                    .doOnError(error -> log.error("❌ Error al parsear request body", error))
                    .flatMap(req -> {
                        log.info("🔍 Iniciando validación del request");
                        return validarRequest(req);
                    })
                    .map(req -> {
                        log.info("🗺️ Mapeando request a DTO de dominio");
                        return requestMapper.toGuardarSolicitudCambioDTO(req);
                    })
                    .doOnNext(dto -> {
                        // Setear el userId extraído del header en el DTO
                        dto.setIdUsuario(Integer.valueOf(userId));
                        log.info("🔄 DTO mapeado - idPartida: {}, userId: {}", dto.getIdPartida(), userId);
                    })
                    .flatMap(dto -> {
                        log.info("📞 Invocando use case guardarSolicitudCambioServicio");
                        return guardarSolicitudCambioUseCase.guardarSolicitudCambioServicio(dto)
                                .doOnSubscribe(sub -> log.info("📡 Suscripción iniciada al use case"))
                                .doOnNext(resp -> log.info("✅ Use case completado exitosamente"))
                                .doOnError(error -> log.error("❌ Error en use case: {}", error.getMessage(), error));
                    })
                    .flatMap(response -> {
                        log.info("✅ Solicitud guardada exitosamente - Preparando respuesta HTTP");
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response);
                    })
                    .onErrorResume(IllegalArgumentException.class, ex -> {
                        log.warn("⚠️ Error de validación: {}", ex.getMessage());
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "success", false,
                                        "error", "Datos inválidos",
                                        "message", ex.getMessage()));
                    })
                    .onErrorResume(ex -> {
                        log.error("❌ Error inesperado en handler: {}", ex.getClass().getName(), ex);
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "success", false,
                                        "error", "Error interno del servidor",
                                        "message", ex.getMessage() != null ? ex.getMessage() : "Error desconocido"));
                    });
        } catch (Exception ex) {
            log.error("❌ Error crítico al iniciar handler: {}", ex.getMessage(), ex);
            return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "success", false,
                            "error", "Error crítico",
                            "message", "No se pudo procesar la solicitud"));
        }
    }

    /**
     * Valida el request usando Jakarta Bean Validation
     *
     * @param req Request a validar
     * @return Mono con el request validado o error
     */
    private Mono<GuardarSolicitudCambioRequest> validarRequest(GuardarSolicitudCambioRequest req) {
        var violations = validator.validate(req);

        if (!violations.isEmpty()) {
            String errores = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            log.warn("⚠️ Errores de validación en el request: {}", errores);
            return Mono.error(new IllegalArgumentException("Errores de validación: " + errores));
        }

        log.debug("✅ Request validado correctamente");
        return Mono.just(req);
    }
}
