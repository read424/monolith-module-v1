package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.DetalleIngresoRollos;
import com.walrex.module_partidas.domain.model.dto.ConsultarDetalleIngresoRequest;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para consultar detalle de ingreso con rollos
 * Define el contrato para la consulta de rollos disponibles por partida y
 * almacén
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ConsultarDetalleIngresoUseCase {

    /**
     * Consulta el detalle de ingreso con rollos disponibles para una partida en un
     * almacén específico
     * 
     * @param request Criterios de consulta (partida y almacén)
     * @return Mono de detalles de ingreso con rollos
     */
    Mono<DetalleIngresoRollos> consultarDetalleIngreso(ConsultarDetalleIngresoRequest request);
}
