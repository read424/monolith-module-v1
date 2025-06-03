package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.Articulo;
import reactor.core.publisher.Mono;

public interface ObtenerArticuloPort {
    Mono<Articulo> obtenerArticuloPorId(Integer idAlmacen, Integer idArticulo);
}
