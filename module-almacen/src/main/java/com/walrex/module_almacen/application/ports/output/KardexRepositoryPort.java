package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface KardexRepositoryPort {
    Mono<List<KardexArticuloDTO>> consultarMovimientosKardex(CriteriosBusquedaKardex consulta);
}
