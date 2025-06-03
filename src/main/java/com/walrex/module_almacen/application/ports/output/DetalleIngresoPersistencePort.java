package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.DetalleIngresoDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DetalleIngresoPersistencePort {
    /**
     * Guarda un detalle de ingreso
     * @param detalleIngresoDTO datos del detalle de ingreso
     * @return el detalle de ingreso guardado con su ID generado
     */
    Mono<DetalleIngresoDTO> guardarDetalleIngreso(DetalleIngresoDTO detalleIngresoDTO);

    /**
     * Guarda múltiples detalles de ingreso para una orden
     * @param detallesIngresoDTO lista de detalles de ingreso
     * @param idOrdenIngreso identificador de la orden de ingreso
     * @return lista de detalles guardados
     */
    Flux<DetalleIngresoDTO> guardarDetallesIngreso(List<DetalleIngresoDTO> detallesIngresoDTO, Long idOrdenIngreso);

    /**
     * Busca todos los detalles asociados a una orden de ingreso
     * @param idOrdenIngreso identificador de la orden de ingreso
     * @return flujo de detalles encontrados
     */
    Flux<DetalleIngresoDTO> buscarDetallesPorOrdenIngreso(Long idOrdenIngreso);
}
