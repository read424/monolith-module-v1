package com.walrex.module_articulos.domain.service;

import com.walrex.module_articulos.application.ports.input.GetArticulosUseCase;
import com.walrex.module_articulos.application.ports.input.SearchArticuloUseCase;
import com.walrex.module_articulos.application.ports.output.ArticuloOutputPort;
import com.walrex.module_articulos.application.ports.output.ArticuloProducerOutputPort;
import com.walrex.module_articulos.domain.model.Articulo;
import com.walrex.module_articulos.domain.model.ArticuloSearchCriteria;
import com.walrex.module_articulos.domain.model.dto.ItemArticuloResponse;
import com.walrex.module_articulos.domain.model.dto.ListArticulosDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticuloService implements GetArticulosUseCase, SearchArticuloUseCase {
    private final ArticuloOutputPort articuloOutputPort;//interfaz repository
    private final ArticuloProducerOutputPort articuloProducerOutputPort;//interfaz producer

    @Override
    public Mono<ListArticulosDataDto> getArticulosByCodigos(List<String> codigos, String correlationId) {
        return Flux.fromIterable(codigos)
                .flatMap(articuloOutputPort::searchByCodeArticulo)
                .map(articulo -> ItemArticuloResponse.builder()
                        .id_articulo(articulo.getIdArticulo().intValue())
                        .cod_articulo(articulo.getCodArticulo())
                        .desc_articulo(articulo.getDescArticulo())
                        .build()
                )
                .collectList()
                .map(items -> ListArticulosDataDto.builder()
                        .articulos_ids(items)
                        .build()
                )
                .flatMap(responseDto-> {
                    if(!responseDto.getArticulos_ids().isEmpty()){
                        return articuloProducerOutputPort.sendArticulosResponse(responseDto, correlationId)
                                .thenReturn(responseDto);
                    }
                    return Mono.just(responseDto);
                })
                .defaultIfEmpty(ListArticulosDataDto.builder()
                        .articulos_ids(new ArrayList<>())
                        .build()
                );
    }

    @Override
    public Flux<Articulo> searchArticulos(ArticuloSearchCriteria criteria) {
        log.info("Buscando artículos con criterio: {}", criteria);
        String searchTerm = criteria.getSearch().toLowerCase().trim();

        // Verificar si el término de búsqueda ya contiene comodines SQL
        boolean containsWildcards = searchTerm.contains("%") || searchTerm.contains("_");

        // Solo agregar los comodines si no existen ya en el término de búsqueda
        String finalSearchTerm = containsWildcards
                ? searchTerm
                : "%" + searchTerm + "%";

        log.info("Término de búsqueda final: {}", finalSearchTerm);

        // Delegar al output port para realizar la búsqueda
        Flux<Articulo> result = criteria.getIdTipoProducto() != null
                ? articuloOutputPort.findByNombreLikeIgnoreCaseAndFamily(
                        finalSearchTerm,
                        criteria.getSize(),
                        criteria.getPage(),
                        criteria.getIdTipoProducto())
                : articuloOutputPort.findByNombreLikeIgnoreCaseOrderByNombre(
                        finalSearchTerm,
                        criteria.getPage(),
                        criteria.getSize());

        return result
                .doOnComplete(() -> log.info("Búsqueda de artículos completada"))
                .doOnError(error -> log.error("Error al buscar artículos", error));
    }
}