package com.walrex.module_driver.application.ports.input;

import com.walrex.module_driver.domain.model.BuscarConductorModel;
import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.domain.model.dto.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DriverCommandUseCase {

    Mono<CreateDriverDTO> crear_conductor(DriverDomain driver, Integer idUsuario);

    Mono<CreateDriverDTO> actualizar_conductor(Integer id, DriverDomain driverDto, Integer idUsuario);

    Mono<Void> deshabilitar_conductor(Integer id, Integer idUsuario);

    Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id);

    /**
     * Busca los datos de un conductor por número de documento y tipo de documento.
     * Método original mantenido para compatibilidad hacia atrás.
     * 
     * @param buscarConductorModel Modelo con número de documento y tipo de documento
     * @return Flux con los datos del conductor encontrado
     */
    Flux<ConductorDataDTO> buscarDatosDeConductorByNumDocAndIdTipDoc(BuscarConductorModel buscarConductorModel);

    /**
     * Busca conductores usando parámetros dinámicos.
     * Nuevo método para búsquedas avanzadas que incluye búsqueda por nombre.
     * 
     * @param searchDriverByParameters Parámetros de búsqueda dinámicos
     * @return Flux con los datos de los conductores encontrados
     */
    Flux<ConductorDataDTO> buscarConductorPorParametros(SearchDriverByParameters searchDriverByParameters);
}
