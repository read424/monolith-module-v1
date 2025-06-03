package com.walrex.module_articulos.domain.service;

import com.walrex.module_articulos.application.ports.input.ArticuloGraphQLInputPort;
import com.walrex.module_articulos.application.ports.output.ArticuloOutputPort;
import com.walrex.module_articulos.domain.model.Articulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticuloGraphQLService  implements ArticuloGraphQLInputPort {
    private final ArticuloOutputPort articuloOutputPort;

    @Override
    public Flux<Articulo> searchArticulos(String query, int page, int size) {
        log.info("Servicio GraphQL - Buscando artículos con query: {}, page: {}, size: {}", query, page, size);
        return articuloOutputPort.findByNombreLikeIgnoreCaseOrderByNombre(query, page, size);
    }

    @Override
    public Mono<Articulo> createArticulo(Articulo articulo) {
        log.info("GraphQL Service - Creando nuevo artículo: {}", articulo);

        // Validar los campos obligatorios
        if (articulo.getCodArticulo() == null || articulo.getCodArticulo().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El código del artículo es obligatorio"));
        }

        if (articulo.getDescArticulo() == null || articulo.getDescArticulo().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La descripción del artículo es obligatoria"));
        }

        // Por defecto, activar el artículo si no se especifica un estado
        if (articulo.getStatus() == null) {
            articulo.setStatus(1); // Asumiendo que 1 es activo
        }

        return articuloOutputPort.save(articulo)
                .doOnSuccess(saved -> log.info("Artículo creado con éxito: {}", saved.getIdArticulo()))
                .doOnError(error -> log.error("Error al crear artículo", error));
    }
}
