package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.CrearOrdenIngresoUseCase;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.OrdenIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.OrdenIngresoLogisticaRequestDto;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import com.walrex.module_security_commons.infrastructure.adapters.JwtUserContextService;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.ResponseCreateOrdenIngresoLogisticaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenIngresoLogisticaHandler {
    private final Validator validator;
    private final CrearOrdenIngresoUseCase crearOrdenIngresoUseCase;
    private final OrdenIngresoLogisticaMapper ordenIngresoMapper;
    private final JwtUserContextService jwtService;

    public Mono<ServerResponse> nuevoIngresoLogistica(ServerRequest request){
        JwtUserInfo user = jwtService.getCurrentUser(request);
        log.info("Método HTTP: {}", request.method());
        log.info("Headers: {}", request.headers().asHttpHeaders());

        return request.bodyToMono(OrdenIngresoLogisticaRequestDto.class)
                .doOnNext(dto->log.info("Request body recibido: {}", dto))
                .flatMap(this::validate)
                .map(ordenIngresoMapper::toOrdenIngreso)
                .doOnNext(ordenIngreso -> {
                    ordenIngreso.setIdUser(Integer.valueOf(user.getUserId()));
                })
                .flatMap(crearOrdenIngresoUseCase::crearOrdenIngresoLogistica)
                .map(this::mapToResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                );
    }

    private Mono<OrdenIngresoLogisticaRequestDto> validate(OrdenIngresoLogisticaRequestDto dto) {
        var errors = new BeanPropertyBindingResult(dto, OrdenIngresoLogisticaRequestDto.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            // Construir mensaje de error
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }

    // ✅ Método para mapear la respuesta
    private ResponseCreateOrdenIngresoLogisticaDto mapToResponse(OrdenIngreso ordenIngreso) {
        return ResponseCreateOrdenIngresoLogisticaDto.builder()
                .success(true)
                .affected_rows(1)
                .message("Orden de ingreso creada exitosamente " + ordenIngreso.getCod_ingreso())
                .build();
    }
}
