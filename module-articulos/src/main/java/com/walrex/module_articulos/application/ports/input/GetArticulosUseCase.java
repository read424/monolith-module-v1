package com.walrex.module_articulos.application.ports.input;

import com.walrex.module_articulos.domain.model.dto.ListArticulosDataDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GetArticulosUseCase {
    Mono<ListArticulosDataDto> getArticulosByCodigos(List<String> codigo, String correlationId);
}
