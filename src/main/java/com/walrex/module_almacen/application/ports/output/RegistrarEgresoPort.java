package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface RegistrarEgresoPort {
    /**
     * Registra una orden de egreso con sus detalles
     *
     * @param idMotivo Motivo del egreso
     * @param idAlmacen Almacén origen
     * @param fecha Fecha del egreso
     * @param observacion Observación
     * @param items Ítems a egresar
     * @param transactionId Identificador de transacción para trazabilidad
     * @return Mono con la orden de egreso creada
     */
    Mono<OrdenEgresoDTO> registrarEgreso(
            Integer idMotivo,
            Integer idAlmacen,
            LocalDate fecha,
            String observacion,
            List<DetalleEgresoDTO> items,
            String transactionId
    );
}
