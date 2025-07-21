package com.walrex.module_driver.application.ports.output;

import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;

import reactor.core.publisher.Mono;

public interface DriverPersistencePort {
    Mono<CreateDriverDTO> guardar_conductor(CreateDriverDTO driverDto);

    Mono<CreateDriverDTO> actualizar_datos_conductor(CreateDriverDTO driverDto);

    Mono<Void> disabled_conductor(Integer id);

    Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id);
}
