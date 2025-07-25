package com.walrex.module_driver.application.ports.input;

import com.walrex.module_driver.domain.model.BuscarConductorModel;
import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DriverCommandUseCase {

    Mono<CreateDriverDTO> crear_conductor(DriverDomain driver, Integer idUsuario);

    Mono<CreateDriverDTO> actualizar_conductor(Integer id, DriverDomain driverDto, Integer idUsuario);

    Mono<Void> deshabilitar_conductor(Integer id, Integer idUsuario);

    Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id);

    /**
     * Busca los datos de un conductor por número de documento y tipo de documento.
     * 
     * @param numDoc   Número de documento del conductor
     * @param idTipDoc ID del tipo de documento
     * @return Mono con los datos del conductor encontrado
     */
    Flux<ConductorDataDTO> buscarDatosDeConductorByNumDocAndIdTipDoc(BuscarConductorModel buscarConductorModel);
}
