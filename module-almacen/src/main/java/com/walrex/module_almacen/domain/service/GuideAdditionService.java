package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.GuideAdditionUseCase;
import com.walrex.module_almacen.application.ports.output.GuideAdditionOutputPort;
import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
import com.walrex.module_almacen.domain.model.dto.AddGuideResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideAdditionService implements GuideAdditionUseCase {

    private final GuideAdditionOutputPort outputPort;

    @Override
    public Mono<AddGuideResponse> addGuide(AddGuideRequest request) {
        log.info("Iniciando proceso de agregado de guía: {} - {}", request.getNu_serie(), request.getNu_comprobante());
        // Aquí se podrían añadir validaciones de negocio adicionales
        return outputPort.saveGuide(request)
                .doOnSuccess(response -> log.info("Guía agregada exitosamente con código: {}", response.getCod_ingreso()));
    }
}
