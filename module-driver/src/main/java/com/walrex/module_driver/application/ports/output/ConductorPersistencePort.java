package com.walrex.module_driver.application.ports.output;

import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para la persistencia de datos de conductores.
 */
public interface ConductorPersistencePort {

    /**
     * Busca un conductor por número de documento y tipo de documento.
     * 
     * @param numDoc   Número de documento del conductor
     * @param idTipDoc ID del tipo de documento
     * @return Mono con los datos del conductor encontrado
     */
    Mono<ConductorDataDTO> buscarConductorPorDocumento(String numDoc, Integer idTipDoc);
}