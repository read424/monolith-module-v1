package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import reactor.core.publisher.Mono;

public interface OrdenSalidaAdapterFactory {
    /**
     * Obtiene el adaptador apropiado según el tipo de orden de salida
     * @param tipoOrden tipo de orden de salida
     * @return adaptador correspondiente
     */
    Mono<OrdenSalidaLogisticaPort> getAdapter(TipoOrdenSalida tipoOrden);

    Mono<OrdenSalidaAprobacionPort> getAprobacionAdapter(TipoOrdenSalida tipoOrden);
}
