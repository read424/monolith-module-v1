package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrdenSalidaLogisticaPort {
    /**
     * Guarda una orden de salida con sus detalles
     * @param ordenSalida datos de la orden de salida
     * @return información de la orden creada
     */
    Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida);

    /**
     * Actualiza el estado de entrega de una orden de salida
     * @param ordenEgresoDTO de la orden
     * @return orden actualizada
     */
    Mono<OrdenEgresoDTO> actualizarEstadoEntrega(OrdenEgresoDTO ordenEgresoDTO);

    /**
     * Procesa la salida por lotes
     * @param ordenSalida orden a procesar
     * @return orden procesada
     */
    Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida);

    Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida);
}
