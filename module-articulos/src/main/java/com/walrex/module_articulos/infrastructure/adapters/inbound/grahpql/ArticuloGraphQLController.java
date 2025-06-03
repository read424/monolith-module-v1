package com.walrex.module_articulos.infrastructure.adapters.inbound.grahpql;

import com.walrex.module_articulos.application.ports.input.ArticuloGraphQLInputPort;
import com.walrex.module_articulos.domain.model.Articulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ArticuloGraphQLController {
    private final ArticuloGraphQLInputPort articuloGraphQLInputPort;

    @QueryMapping
    public Flux<Articulo> searchArticulos(
            @Argument String query,
            @Argument int page,
            @Argument int size
        ){
        log.info("Graphql Query - searchArticulos con query: {}, page: {}, size: {}", query, page, size);
        String formattedQuery = "%"+query.toLowerCase()+"%";
        return articuloGraphQLInputPort.searchArticulos(formattedQuery, page, size);
    }

    @MutationMapping
    public Mono<Articulo> createArticulo(@Argument("input") Articulo input) {
        log.info("GraphQL Mutation - createArticulo con input: {}", input);
        return articuloGraphQLInputPort.createArticulo(input);
    }
}
