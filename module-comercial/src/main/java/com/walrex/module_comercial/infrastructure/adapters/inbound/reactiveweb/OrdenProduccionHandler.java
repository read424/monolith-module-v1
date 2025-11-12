package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_comercial.application.ports.input.GetOrdenProduccionPartidaUseCase;
import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenProduccionHandler {

    private final GetOrdenProduccionPartidaUseCase getOrdenProduccionPartidaUseCase;
    private final UserContextProvider userContextProvider;

    /**
     * Handler para obtener orden de producción por partida
     * GET /comercial/orden-produccion-partida/{idPartida}
     */
    public Mono<ServerResponse> getOrdenProduccionPorPartida(ServerRequest request) {
        // Extraer información del usuario desde los headers usando module-security-commons
        JwtUserInfo userInfo = userContextProvider.getCurrentUser(request);
        String userId = userInfo.getUserId();

        Integer idPartida = Integer.valueOf(request.pathVariable("idPartida"));
        log.info("🔍 GET /comercial/orden-produccion-partida/{} - Usuario: {}", idPartida, userId);

        return getOrdenProduccionPartidaUseCase.getInfoOrdenProduccionPartida(idPartida)
                .flatMap(resultado -> {
                    log.info("✅ Orden de producción obtenida con éxito para idPartida: {}", idPartida);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(resultado);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "Datos inválidos",
                                    "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al obtener orden de producción por partida: ", ex);
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "Error interno del servidor",
                                    "message", "No se pudo obtener la orden de producción"));
                });
    }
}
