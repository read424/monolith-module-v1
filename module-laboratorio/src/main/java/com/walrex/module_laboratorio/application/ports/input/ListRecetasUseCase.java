package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.PagedResponse;
import com.walrex.module_laboratorio.domain.model.Receta;
import reactor.core.publisher.Mono;

public interface ListRecetasUseCase {
    Mono<PagedResponse<Receta>> listAll(String search, int page, int size);
    Mono<Receta> getCurvaDisenoById(Integer id);
    Mono<Receta> updateCurvaDiseno(Integer id, String curvaDiseno);
    Mono<byte[]> generateCurvaDisenoPdf(Integer id);
}
