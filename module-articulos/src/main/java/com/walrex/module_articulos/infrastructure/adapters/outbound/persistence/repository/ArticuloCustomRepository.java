package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import reactor.core.publisher.Flux;

public interface ArticuloCustomRepository {
    Flux<ArticuloEntity> findByNombreLikeIgnoreCaseAndFamily(String query, int size, int offset, Integer idTipoProducto);
}
