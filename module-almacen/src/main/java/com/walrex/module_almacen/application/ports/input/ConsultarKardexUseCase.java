package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.KardexReporte;
import reactor.core.publisher.Mono;


public interface ConsultarKardexUseCase {
    Mono<KardexReporte> consultarKardex(CriteriosBusquedaKardex request);
}
