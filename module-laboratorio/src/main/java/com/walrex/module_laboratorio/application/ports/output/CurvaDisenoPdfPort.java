package com.walrex.module_laboratorio.application.ports.output;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.domain.model.Receta;
import reactor.core.publisher.Mono;

public interface CurvaDisenoPdfPort {
    Mono<byte[]> generatePdf(Receta receta);

    Mono<byte[]> generatePdf(CurvaDiseno curvaDiseno);
}
