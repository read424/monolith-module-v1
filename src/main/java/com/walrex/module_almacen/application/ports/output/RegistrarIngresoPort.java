package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoDTO;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface RegistrarIngresoPort {
    /**
     * Registra una orden de ingreso con sus detalles
     *
     * @param idMotivo Motivo del ingreso
     * @param idAlmacen Almacén destino
     * @param fecha Fecha del ingreso
     * @param observacion Observación
     * @param items Ítems a ingresar
     * @param transactionId Identificador de transacción para trazabilidad
     * @return Mono con la orden de ingreso creada
     */
    Mono<OrdenIngresoDTO> registrarIngreso(
            Integer idMotivo,
            Integer idAlmacen,
            LocalDate fecha,
            String observacion,
            List<ItemProductDTO> items,
            String transactionId
    );
}
