package com.walrex.module_articulos.application.ports.input;

import com.walrex.module_articulos.domain.model.Articulo;
import com.walrex.module_articulos.domain.model.ArticuloSearchCriteria;
import reactor.core.publisher.Flux;

public interface SearchArticuloUseCase {
    Flux<Articulo> searchArticulos(ArticuloSearchCriteria criteria);
}
