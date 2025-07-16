package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.module_ecomprobantes.domain.model.dto.TipoSerieDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de persistencia de TipoSerie.
 * 
 * Define el contrato para acceder a los datos de series de comprobantes.
 */
public interface TipoSeriePersistencePort {

    /**
     * Guarda una nueva serie
     * 
     * @param tipoSerieDTO DTO con los datos de la serie
     * @return Mono con la serie guardada
     */
    Mono<TipoSerieDTO> guardarTipoSerie(TipoSerieDTO tipoSerieDTO);

    /**
     * Busca una serie por su ID
     * 
     * @param idSerie Identificador de la serie
     * @return Mono con la serie encontrada o vacío si no existe
     */
    Mono<TipoSerieDTO> buscarTipoSeriePorId(Integer idSerie);

    /**
     * Busca una serie por su número de serie
     * 
     * @param nuSerie Número de serie
     * @return Mono con la serie encontrada o vacío si no existe
     */
    Mono<TipoSerieDTO> buscarTipoSeriePorNumero(String nuSerie);

    /**
     * Busca todas las series por tipo de comprobante
     * 
     * @param idCompro Identificador del tipo de comprobante
     * @return Flux con las series encontradas
     */
    Flux<TipoSerieDTO> buscarSeriesPorTipoComprobante(Integer idCompro);

    /**
     * Busca series activas por tipo de comprobante
     * 
     * @param idCompro Identificador del tipo de comprobante
     * @return Flux con las series activas encontradas
     */
    Flux<TipoSerieDTO> buscarSeriesActivasPorTipoComprobante(Integer idCompro);

    /**
     * Busca series que son comprobantes electrónicos (CPE)
     * 
     * @param isCpe Indicador CPE (0 = No CPE, 1 = CPE)
     * @return Flux con las series encontradas
     */
    Flux<TipoSerieDTO> buscarSeriesPorTipoCPE(Integer isCpe);

    /**
     * Actualiza una serie existente
     * 
     * @param tipoSerieDTO DTO con los datos actualizados
     * @return Mono con la serie actualizada
     */
    Mono<TipoSerieDTO> actualizarTipoSerie(TipoSerieDTO tipoSerieDTO);

    /**
     * Elimina una serie por su ID
     * 
     * @param idSerie Identificador de la serie
     * @return Mono<Void> cuando se completa la eliminación
     */
    Mono<Void> eliminarTipoSerie(Integer idSerie);
}