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
import com.walrex.module_almacen.application.ports.input.RegistrarDevolucionRollosUseCase;
import com.walrex.module_almacen.domain.model.JwtUserInfo;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.*;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarRollosDisponiblesRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.RegistrarDevolucionRollosRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.ConsultarRollosDisponiblesResponse;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.RegistrarDevolucionRollosResponse;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.JwtUserContextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para operaciones de devolución de rollos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollosDevolucionHandler {

        private final Validator validator;
        private final ConsultarRollosDisponiblesDevolucionUseCase consultarRollosUseCase;
        private final RegistrarDevolucionRollosUseCase registrarDevolucionUseCase;
        private final ConsultarRollosDisponiblesRequestMapper requestMapper;
        private final RegistrarDevolucionRollosRequestMapper devolucionRequestMapper;
        private final RegistrarDevolucionRollosResponseMapper responseMapper;
        private final JwtUserContextService jwtUserContextService;

        public Mono<ServerResponse> consultarRollosDisponibles(ServerRequest request) {
                log.info("🔍 Consultando rollos disponibles para devolución - Método HTTP: {}", request.method());
                log.info("🔍 Query params: {}", request.queryParams());

                return Mono.just(requestMapper.extractFromQuery(request))
                                .doOnNext(dto -> log.info("Request params recibidos: {}", dto))
                                .flatMap(this::validarRequest)
                                .flatMap(requestDto -> consultarRollosUseCase
                                                .consultarRollosDisponiblesResponse(requestDto.getIdCliente(),
                                                                requestDto.getIdArticulo()))
                                .flatMap(response -> ServerResponse.status(HttpStatus.OK)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(response))
                                .onErrorResume(this::manejarErrores);
        }

        public Mono<ServerResponse> crearDevolucionRollos(ServerRequest request) {
                log.info("🔄 Creando devolución de rollos - Método HTTP: {}", request.method());

                JwtUserInfo user = jwtUserContextService.getCurrentUser(request);
                Integer idUsuario = Integer.valueOf(user.getUserId());

                return request.bodyToMono(RegistrarDevolucionRollosRequest.class)
                                .doOnNext(dto -> log.info("Request body recibido: {}", dto))
                                .flatMap(this::validarRequestDevolucion)
                                .map(devolucionRequestMapper::requestToDto)
                                .doOnNext(dto -> log.debug("DTO mapeado: {}", dto))
                                .flatMap(devolucionDto -> registrarDevolucionUseCase
                                                .registrarDevolucionRollos(devolucionDto, idUsuario))
                                .map(responseMapper::toResponse)
                                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(response))
                                .onErrorResume(this::manejarErroresDevolucion);
        }

        private Mono<ConsultarRollosDisponiblesRequest> validarRequest(ConsultarRollosDisponiblesRequest dto) {
                var errors = new BeanPropertyBindingResult(dto, ConsultarRollosDisponiblesRequest.class.getName());
                validator.validate(dto, errors);
                if (errors.hasErrors()) {
                        var errorMessages = errors.getFieldErrors().stream()
                                        .map(error -> String.format("Campo '%s': %s", error.getField(),
                                                        error.getDefaultMessage()))
                                        .toList();
                        return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
                }
                return Mono.just(dto);
        }

        private Mono<RegistrarDevolucionRollosRequest> validarRequestDevolucion(RegistrarDevolucionRollosRequest dto) {
                var errors = new BeanPropertyBindingResult(dto, RegistrarDevolucionRollosRequest.class.getName());
                validator.validate(dto, errors);
                if (errors.hasErrors()) {
                        var errorMessages = errors.getFieldErrors().stream()
                                        .map(error -> String.format("Campo '%s': %s", error.getField(),
                                                        error.getDefaultMessage()))
                                        .toList();
                        return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
                }
                return Mono.just(dto);
        }

        private Mono<ServerResponse> manejarErrores(Throwable error) {
                log.error("❌ Error al consultar rollos disponibles: {}", error.getMessage(), error);

                HttpStatus status;
                String mensaje;

                // Manejo específico para errores de validación
                if (error instanceof ServerWebInputException) {
                        status = HttpStatus.BAD_REQUEST;
                        mensaje = error.getMessage();
                        log.info("🔍 Error de validación detectado: {}", mensaje);
                } else if (error instanceof IllegalArgumentException) {
                        status = HttpStatus.BAD_REQUEST;
                        mensaje = error.getMessage();
                } else {
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                        mensaje = "Error interno del servidor: " + error.getMessage();
                }

                var response = ConsultarRollosDisponiblesResponse.builder()
                                .rollosDisponibles(java.util.List.of())
                                .totalRollos(0)
                                .success(false)
                                .mensaje(mensaje)
                                .build();

                return ServerResponse.status(status)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response);
        }

        private Mono<ServerResponse> manejarErroresDevolucion(Throwable error) {
                log.error("❌ Error al crear devolución: {}", error.getMessage(), error);

                HttpStatus status;
                String mensaje;

                // Manejo específico para errores de validación
                if (error instanceof ServerWebInputException) {
                        status = HttpStatus.BAD_REQUEST;
                        mensaje = error.getMessage();
                        log.info("🔍 Error de validación detectado: {}", mensaje);
                } else if (error instanceof IllegalArgumentException) {
                        status = HttpStatus.BAD_REQUEST;
                        mensaje = error.getMessage();
                } else {
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                        mensaje = "Error interno del servidor: " + error.getMessage();
                }

                var response = RegistrarDevolucionRollosResponse.builder()
                                .codSalida("ERROR")
                                .totalKg(0.0)
                                .totalRollos(0)
                                .mensaje(mensaje)
                                .build();

                return ServerResponse.status(status)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response);
        }
}