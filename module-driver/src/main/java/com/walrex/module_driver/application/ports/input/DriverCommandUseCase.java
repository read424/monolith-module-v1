package com.walrex.module_driver.application.ports.input;

import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;

import reactor.core.publisher.Mono;

public interface DriverCommandUseCase {

    Mono<CreateDriverDTO> crear_conductor(DriverDomain driver, Integer idUsuario);

    Mono<CreateDriverDTO> actualizar_conductor(Integer id, DriverDomain driverDto, Integer idUsuario);

    Mono<Void> deshabilitar_conductor(Integer id, Integer idUsuario);

    Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id);
}
