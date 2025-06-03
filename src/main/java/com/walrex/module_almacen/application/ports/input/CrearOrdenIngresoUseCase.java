package com.walrex.module_almacen.application.ports.input;

import com.walrex.avro.schemas.CreateOrdeningresoMessage;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoResponseDTO;
import reactor.core.publisher.Mono;

public interface CrearOrdenIngresoUseCase {
    /**
     * Crea una nueva orden de ingreso y sus detalles
     * @param ordenIngreso datos de la orden de ingreso
     * @return información de la orden creada
     */
    Mono<OrdenIngreso> crearOrdenIngresoLogistica(OrdenIngreso ordenIngreso);

    /**
     * Procesa un mensaje Avro para crear una nueva orden de ingreso
     * @param message mensaje Avro con la información de la orden
     * @return información de la orden creada
     */
    Mono<OrdenIngresoResponseDTO> procesarMensajeOrdenIngreso(CreateOrdeningresoMessage message);
}
