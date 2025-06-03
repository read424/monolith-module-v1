package com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_articulos.application.ports.input.SearchArticuloUseCase;
import com.walrex.module_articulos.domain.model.ArticuloSearchCriteria;
import com.walrex.module_articulos.domain.model.dto.ArticuloDto;
import com.walrex.module_articulos.infrastructure.adapters.inbound.consumer.mapper.ArticuloSearchMapper;
import com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb.dto.RequestSearchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticuloHandler {
    private final SearchArticuloUseCase searchArticuloUseCase;
    private final ArticuloSearchMapper articuloSearchMapper;

    public Mono<ServerResponse> searchArticulos(ServerRequest request) {
        String query = request.queryParam("query").orElse("");
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        RequestSearchDTO dto_request = RequestSearchDTO.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();

        // Convertimos el DTO a objeto de dominio
        ArticuloSearchCriteria criteria = articuloSearchMapper.dtoToArticuloSearch(dto_request);

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(searchArticuloUseCase.searchArticulos(criteria), ArticuloDto.class);
    }
}
