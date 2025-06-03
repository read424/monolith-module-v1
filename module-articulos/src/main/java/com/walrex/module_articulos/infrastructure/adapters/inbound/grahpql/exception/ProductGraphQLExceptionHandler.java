package com.walrex.module_articulos.infrastructure.adapters.inbound.grahpql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class ProductGraphQLExceptionHandler implements DataFetcherExceptionResolver {
    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment environment) {
        if (exception instanceof IllegalArgumentException) {
            return Mono.just(Collections.singletonList(
                GraphqlErrorBuilder.newError()
                    .message(exception.getMessage())
                    .path(environment.getExecutionStepInfo().getPath())
                    .location(environment.getField().getSourceLocation())
                    .build()
            ));
        }

        // Para otros tipos de excepciones
        return Mono.just(Collections.singletonList(
            GraphqlErrorBuilder.newError()
                .message("Error interno: " + exception.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build()
        ));
    }
}
