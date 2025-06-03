package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.Kardex;
import reactor.core.publisher.Mono;

public interface GuardarKardexPort {
    Mono<Kardex> guardarKardex(Kardex kardex);
}
