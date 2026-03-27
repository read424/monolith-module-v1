package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import reactor.core.publisher.Mono;

public interface UpdateGuideArticleUseCase {
    Mono<Void> updateGuideArticle(Integer idDetalleOrden, UpdateGuideArticleRequest request);
}
