package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import reactor.core.publisher.Mono;

public interface UpdateGuideArticleOutputPort {
    Mono<Long> findIdOrdenIngresoByIdDetalleOrden(Integer idDetalleOrden);
    Mono<Long> countExistingRolls(Integer idDetalleOrden);
    Mono<Boolean> existsProductionOrderByIdOrdenIngreso(Long idOrdenIngreso);
    Mono<Boolean> existsAssignedPartidaByIdDetalleOrden(Integer idDetalleOrden);
    Mono<Void> updateGuideArticle(Integer idDetalleOrden, UpdateGuideArticleRequest request);
}
