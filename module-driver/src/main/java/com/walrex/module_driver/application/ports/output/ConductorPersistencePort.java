package com.walrex.module_driver.application.ports.output;

import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.SearchDriverByParameters;

import reactor.core.publisher.Flux;

/**
 * Puerto de salida para la persistencia de datos de conductores.
 */
public interface ConductorPersistencePort {

    /**
     * Busca un conductor por número de documento y tipo de documento.
     * 
     * @param numDoc   Número de documento del conductor
     * @param idTipDoc ID del tipo de documento
     * @return Flux con los datos del conductor encontrado
     */
    Flux<ConductorDataDTO> buscarConductorPorDocumento(String numDoc, Integer idTipDoc);

    /**
     * Busca conductores usando parámetros dinámicos.
     * Nuevo método para búsquedas avanzadas que incluye búsqueda por nombre.
     * 
     * @param searchDriverByParameters Parámetros de búsqueda dinámicos
     * @return Flux con los datos de los conductores encontrados
     */
    Flux<ConductorDataDTO> buscarConductorPorParametros(SearchDriverByParameters searchDriverByParameters);
}