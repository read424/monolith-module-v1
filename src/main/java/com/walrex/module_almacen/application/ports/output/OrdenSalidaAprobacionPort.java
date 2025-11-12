package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.ArticuloRequerimiento;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrdenSalidaAprobacionPort  extends OrdenSalidaLogisticaPort{
    Mono<OrdenEgresoDTO> procesarAprobacionCompleta(
            AprobarSalidaRequerimiento request,
            List<ArticuloRequerimiento> productosSeleccionados
    );
}
