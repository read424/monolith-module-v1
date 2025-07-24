package com.walrex.module_ecomprobantes.application.ports.input;

import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;

import reactor.core.publisher.Mono;

/**
 * Use Case para consultar información de un comprobante por su ID
 * Puerto de entrada que define el contrato para obtener datos del comprobante
 */
public interface ConsultarComprobanteUseCase {

        /**
         * Consulta la información completa de un comprobante por su ID
         * 
         * @param idComprobante ID único del comprobante
         * @return Mono con la información del comprobante
         */
        Mono<ComprobanteDTO> consultarComprobante(String idComprobante);

}