package com.walrex.module_laboratorio.application.ports.input;

import reactor.core.publisher.Mono;

public interface GenerateCurvaDisenoPdfUseCase {
    Mono<byte[]> generatePdf(Integer id);
}
