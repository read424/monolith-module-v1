package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_driver.application.ports.input.DriverCommandUseCase;
import com.walrex.module_driver.domain.model.JwtUserInfo;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.mapper.*;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request.CreateDriverRequest;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request.SearchConductorRequest;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response.ConductorResponse;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response.ListConductorSearchResponse;
import com.walrex.module_driver.infrastructure.adapters.inbound.rest.JwtUserContextDriverService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverHandler {

    private final DriverCommandUseCase driverCommandUseCase;
    private final JwtUserContextDriverService jwtService;
    private final DriverRequestMapper driverRequestMapper;
    private final SearchDriverRequestMapper searchDriverRequestMapper;
    private final ConductorDataResponseMapper conductorDataResponseMapper;

    public Mono<ServerResponse> createDriver(ServerRequest request) {
        log.info("📥 POST /driver/ payload: {}", request);
        JwtUserInfo user = jwtService.getCurrentUser(request);
        log.info("Método HTTP: {}", request.method());
        log.info("Headers: {}", request.headers().asHttpHeaders());

        return request.bodyToMono(CreateDriverRequest.class)
                .map(driverRequestMapper::toDomain)
                .doOnNext(domain -> log.info("🛠️ Mapeo a domain exitoso: {}", domain))
                .flatMap(domain -> driverCommandUseCase.crear_conductor(domain, Integer.valueOf(user.getUserId())))
                .flatMap(resultado -> {
                    log.info("✅ Conductor guardado con éxito: {}", resultado);
                    return ServerResponse.ok().bodyValue(resultado);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest().bodyValue(Map.of(
                            "error", "Datos inválidos",
                            "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al crear registro de conductor: ", ex);
                    return ServerResponse.status(500).bodyValue(Map.of(
                            "error", "Error interno del servidor",
                            "message", "No se pudo crear el registro del conductor"));
                });
    }

    public Mono<ServerResponse> updateDriver(ServerRequest request) {
        log.info("📝 PUT /driver/{} payload: {}", request.pathVariable("id"), request);
        JwtUserInfo user = jwtService.getCurrentUser(request);

        Integer driverId = Integer.valueOf(request.pathVariable("id"));

        return request.bodyToMono(CreateDriverRequest.class)
                .map(driverRequestMapper::toDomain)
                .flatMap(domain -> driverCommandUseCase.actualizar_conductor(driverId, domain,
                        Integer.valueOf(user.getUserId())))
                .flatMap(resultado -> {
                    log.info("✅ Conductor actualizado con éxito: {}", resultado);
                    return ServerResponse.ok().bodyValue(resultado);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest().bodyValue(Map.of(
                            "error", "Datos inválidos",
                            "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al actualizar conductor: ", ex);
                    return ServerResponse.status(500).bodyValue(Map.of(
                            "error", "Error interno del servidor",
                            "message", "No se pudo actualizar el conductor"));
                });
    }

    public Mono<ServerResponse> deleteDriver(ServerRequest request) {
        log.info("🗑️ DELETE /driver/{}", request.pathVariable("id"));
        JwtUserInfo user = jwtService.getCurrentUser(request);

        Integer driverId = Integer.valueOf(request.pathVariable("id"));

        return driverCommandUseCase.deshabilitar_conductor(driverId, Integer.valueOf(user.getUserId()))
                .flatMap(resultado -> {
                    log.info("✅ Conductor deshabilitado con éxito, ID: {}", driverId);
                    return ServerResponse.ok().bodyValue(Map.of(
                            "message", "Conductor deshabilitado exitosamente",
                            "id", driverId));
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest().bodyValue(Map.of(
                            "error", "Datos inválidos",
                            "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al deshabilitar conductor: ", ex);
                    return ServerResponse.status(500).bodyValue(Map.of(
                            "error", "Error interno del servidor",
                            "message", "No se pudo deshabilitar el conductor"));
                });
    }

    public Mono<ServerResponse> getDriver(ServerRequest request) {
        log.info("🔍 GET /driver/{}", request.pathVariable("id"));

        Integer driverId = Integer.valueOf(request.pathVariable("id"));

        return driverCommandUseCase.obtener_conductor_por_id(driverId)
                .flatMap(resultado -> {
                    log.info("✅ Conductor obtenido con éxito, ID: {}", driverId);
                    return ServerResponse.ok().bodyValue(resultado);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest().bodyValue(Map.of(
                            "error", "Datos inválidos",
                            "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al obtener conductor: ", ex);
                    return ServerResponse.status(500).bodyValue(Map.of(
                            "error", "Error interno del servidor",
                            "message", "No se pudo obtener el conductor"));
                });
    }

    public Mono<ServerResponse> buscarConductor(ServerRequest request) {
        log.info("🔍 Handler: Iniciando búsqueda de conductor: {}", request);

        return extractSearchParameters(request)
                .map(searchDriverRequestMapper::toDomain)
                .flatMapMany(driverCommandUseCase::buscarDatosDeConductorByNumDocAndIdTipDoc)
                .collectList()
                .map(conductorDataList -> {
                    List<ConductorResponse> responses = conductorDataResponseMapper.toResponseList(conductorDataList);
                    return ListConductorSearchResponse.builder()
                            .conductores(responses)
                            .total(responses.size())
                            .build();
                })
                .flatMap(conductor -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(conductor))
                .doOnSuccess(response -> log.info("✅ Handler: Búsqueda de conductor completada exitosamente"))
                .doOnError(error -> log.error("❌ Handler: Error en búsqueda de conductor: {}", error.getMessage()))
                .onErrorResume(this::handleError);
    }

    private Mono<SearchConductorRequest> extractSearchParameters(ServerRequest request) {
        return Mono.fromCallable(() -> {
            String numDoc = request.queryParam("num_doc").orElse(null);
            Integer idTipDoc = request.queryParam("id_tip_doc")
                    .map(Integer::valueOf)
                    .orElse(null);

            return SearchConductorRequest.builder()
                    .numDoc(numDoc)
                    .idTipDoc(idTipDoc)
                    .build();
        });
    }

    /**
     * Maneja los errores de forma reactiva.
     */
    private Mono<ServerResponse> handleError(Throwable error) {
        log.error("❌ Error en handler de conductor: {}", error.getMessage());

        if (error instanceof IllegalArgumentException) {
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "error", "Parámetros inválidos",
                            "message", error.getMessage()));
        }

        return ServerResponse.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "error", "Error interno del servidor",
                        "message", "Ocurrió un error inesperado"));
    }
}
