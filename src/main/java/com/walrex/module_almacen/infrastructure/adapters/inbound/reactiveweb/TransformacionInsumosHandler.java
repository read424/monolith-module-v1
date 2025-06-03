package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.ProcesarTransformacionUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.OrdenIngresoTransformacionRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.OrdenIngresoTransformacionRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.ResponseCreateOrdenIngresoLogisticaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransformacionInsumosHandler {
    private final ProcesarTransformacionUseCase procesarTransformacionUseCase;
    private final OrdenIngresoTransformacionRequestMapper ordenIngresoTransformacionMapper;
    private final Validator validator;

    public Mono<ServerResponse> crearConversion(ServerRequest request){
        log.info("Método HTTP: {}", request.method());
        log.info("Headers: {}", request.headers().asHttpHeaders());
        return request.bodyToMono(OrdenIngresoTransformacionRequestDto.class)
                .doOnNext(dto->log.info("Request body recibido: {}", dto))
                .flatMap(this::validate)
                .map(ordenIngresoTransformacionMapper::toOrdenIngreso)
                .flatMap(ordenIngreso -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(procesarTransformacionUseCase.procesarTransformacion(ordenIngreso), ResponseCreateOrdenIngresoLogisticaDto.class)
                );
    }

    private Mono<OrdenIngresoTransformacionRequestDto> validate(OrdenIngresoTransformacionRequestDto dto) {
        var errors = new BeanPropertyBindingResult(dto, OrdenIngresoTransformacionRequestDto.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }
}
