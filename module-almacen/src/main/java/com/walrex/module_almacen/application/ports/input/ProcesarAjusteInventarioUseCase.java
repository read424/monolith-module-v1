package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.RequestAjusteInventoryDTO;
import com.walrex.module_almacen.domain.model.dto.ResponseAjusteInventoryDTO;
import reactor.core.publisher.Mono;

public interface ProcesarAjusteInventarioUseCase {
    /**
     * Procesa un ajuste de inventario, registrando tanto ingresos como egresos
     *
     * @param ajusteInventoryDTO DTO con la información del ajuste de inventario
     * @param correlationId ID de correlación para rastreo (puede ser null)
     * @return Mono con la respuesta del procesamiento
     */
    Mono<ResponseAjusteInventoryDTO> procesarAjusteInventario(
            RequestAjusteInventoryDTO ajusteInventoryDTO,
            String correlationId);
}
