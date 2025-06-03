package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.AprobarSalidaInsumosUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaRequestDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.ApproveDeliverRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.OrdenIngresoLogisticaRequestDto;
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
public class ApproveDeliveryHandler {
    private final Validator validator;
    private final AprobarSalidaInsumosUseCase aprobarSalidaInsumosUseCase;
    private final ApproveDeliverRequestMapper approveDeliverRequestMapper;

    public Mono<ServerResponse> deliver(ServerRequest request){
        log.info("Método HTTP: {}", request.method());
        log.info("Headers: {}", request.headers().asHttpHeaders());

        return request.bodyToMono(AprobarSalidaRequestDTO.class)
                .doOnNext(dto->log.info("Request body recibido: {}", dto))
                .flatMap(this::validate)
                .map(approveDeliverRequestMapper::toDomain)
                .flatMap(requestDelivered -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(aprobarSalidaInsumosUseCase.aprobarSalidaInsumos(requestDelivered), ResponseCreateOrdenIngresoLogisticaDto.class)
                );

    }

    private Mono<AprobarSalidaRequestDTO> validate(AprobarSalidaRequestDTO dto) {
        var errors = new BeanPropertyBindingResult(dto, AprobarSalidaRequestDTO.class.getName());
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
}
