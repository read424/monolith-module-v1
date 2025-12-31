package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.GuiaStatusProjection;

import reactor.core.publisher.Flux;

/**
 * Puerto de salida para consultar status de guías según rollos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface GuiaStatusPort {

    /**
     * Obtiene todas las guías con sus status calculados a partir de los rollos
     * Filtra por almacén de tela cruda (id_almacen=2) y motivo ingreso (id_motivo=1)
     *
     * @return Flux de proyecciones con información de guías y status
     */
    Flux<GuiaStatusProjection> obtenerGuiasConStatusRollos();
}
