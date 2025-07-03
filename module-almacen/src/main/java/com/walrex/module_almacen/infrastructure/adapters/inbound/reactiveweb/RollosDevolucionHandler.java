package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import com.walrex.module_almacen.application.ports.input.ConsultarRollosDisponiblesDevolucionUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.ConsultarRollosDisponiblesRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarRollosDisponiblesRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.ConsultarRollosDisponiblesResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para consultar rollos disponibles para devolución
 * Sigue el patrón HandlerFunction de WebFlux
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollosDevolucionHandler {

    private final Validator validator;
    private final ConsultarRollosDisponiblesDevolucionUseCase consultarRollosUseCase;
    private final ConsultarRollosDisponiblesRequestMapper requestMapper;

    public Mono<ServerResponse> consultarRollosDisponibles(ServerRequest request) {
        log.info("🔍 Consultando rollos disponibles para devolución - Método HTTP: {}", request.method());
        log.info("🔍 Query params: {}", request.queryParams());

        return Mono.just(requestMapper.extractFromQuery(request))
                .doOnNext(dto -> log.info("Request params recibidos: {}", dto))
                .flatMap(this::validarRequest)
                .flatMap(requestDto -> consultarRollosUseCase
                        .consultarRollosDisponibles(requestDto.getIdCliente(), requestDto.getIdArticulo())
                        .collectList()
                        .map(rollos -> ConsultarRollosDisponiblesResponse.builder()
                                .rollosDisponibles(rollos)
                                .totalRollos(rollos.size())
                                .success(true)
                                .mensaje("Rollos disponibles para devolución consultados exitosamente")
                                .build()))
                .flatMap(response -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::manejarErrores);
    }

    private Mono<ConsultarRollosDisponiblesRequest> validarRequest(ConsultarRollosDisponiblesRequest dto) {
        var errors = new BeanPropertyBindingResult(dto, ConsultarRollosDisponiblesRequest.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }

    private Mono<ServerResponse> manejarErrores(Throwable error) {
        log.error("❌ Error al consultar rollos disponibles: {}", error.getMessage(), error);

        var response = ConsultarRollosDisponiblesResponse.builder()
                .rollosDisponibles(java.util.List.of())
                .totalRollos(0)
                .success(false)
                .mensaje("Error al consultar rollos disponibles: " + error.getMessage())
                .build();

        HttpStatus status = error instanceof IllegalArgumentException
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }
}