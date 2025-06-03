package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import reactor.core.publisher.Flux;

import java.util.List;

public interface GuardarDetalleOrdenIngresoPort {
    Flux<DetalleOrdenIngreso> guardarDetallesOrdenIngreso(List<DetalleOrdenIngreso> detalles, Integer idOrdenIngreso);
}
