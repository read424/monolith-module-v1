package com.walrex.module_laboratorio.application.service;

import com.walrex.module_laboratorio.application.ports.input.ListRecetasUseCase;
import com.walrex.module_laboratorio.application.ports.output.CurvaDisenoPdfPort;
import com.walrex.module_laboratorio.application.ports.output.RecetaPersistencePort;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.domain.exceptions.RecetaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecetaService implements ListRecetasUseCase {

    private final RecetaPersistencePort persistencePort;
    private final CurvaDisenoPdfPort curvaDisenoPdfPort;

    @Override
    public Mono<PagedResponse<Receta>> listAll(String search, int page, int size) {
        String term = (search == null) ? "" : search.trim();
        return Mono.zip(
                persistencePort.findAll(term, page, size).collectList(),
                persistencePort.count(term)
        ).map(tuple -> PagedResponse.of(tuple.getT1(), page + 1, size, tuple.getT2()));
    }

    @Override
    public Mono<Receta> getCurvaDisenoById(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new RecetaException("Receta no encontrada", "NOT_FOUND")))
                .flatMap(receta -> {
                    if (receta.getCurvaDiseno() == null || receta.getCurvaDiseno().isBlank()) {
                        return Mono.error(new RecetaException(
                                "La receta no tiene curva_diseno registrada", "CURVA_DISENO_EMPTY"));
                    }
                    return Mono.just(Receta.builder()
                            .id(receta.getId())
                            .curvaDiseno(receta.getCurvaDiseno())
                            .build());
                });
    }

    @Override
    public Mono<Receta> updateCurvaDiseno(Integer id, String curvaDiseno) {
        if (curvaDiseno == null || curvaDiseno.isBlank()) {
            return Mono.error(new RecetaException(
                    "La curva_diseno es obligatoria", "INVALID_CURVA_DISENO"));
        }

        return persistencePort.existsById(id)
                .flatMap(exists -> exists
                        ? persistencePort.updateCurvaDiseno(id, curvaDiseno)
                        : Mono.error(new RecetaException(
                                "Receta no encontrada", "NOT_FOUND")));
    }

    @Override
    public Mono<byte[]> generateCurvaDisenoPdf(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new RecetaException("Receta no encontrada", "NOT_FOUND")))
                .flatMap(receta -> {
                    if (receta.getCurvaDiseno() == null || receta.getCurvaDiseno().isBlank()) {
                        return Mono.error(new RecetaException(
                                "La receta no tiene curva_diseno registrada", "CURVA_DISENO_EMPTY"));
                    }
                    return curvaDisenoPdfPort.generatePdf(receta);
                });
    }
}
