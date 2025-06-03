package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence;

import com.walrex.module_articulos.application.ports.output.ArticuloOutputPort;
import com.walrex.module_articulos.domain.model.Articulo;
import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.mapper.DomainMapper;
import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository.ArticuloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticuloAdapter implements ArticuloOutputPort {
    private final ArticuloRepository articuloRepository;
    private final DomainMapper domainMapper;

    @Override
    public Mono<Articulo> searchByCodeArticulo(String codigo) {
        return articuloRepository.findOneByCodArticulo(codigo)
                .doOnNext(entity -> log.info("Artículo encontrado con código: {}, id: {}",
                        codigo, entity.getId()))
                .map(domainMapper::entityToDomain)
                .doOnError(error -> {
                    if (error.getMessage() != null && error.getMessage().contains("non unique result")) {
                        log.error("ERROR DE INTEGRIDAD: Se encontraron múltiples artículos con código {}. " +
                                        "Esto indica un problema en la base de datos ya que este campo debería ser único.",
                                codigo, error);
                    } else {
                        log.error("Error al buscar artículo con código {}: {}",
                                codigo, error.getMessage(), error);
                    }
                })
                .onErrorResume(NoSuchElementException.class, error -> {
                    log.warn("Artículo con código {} no encontrado", codigo);
                    return Mono.empty();
                });
    }

    @Override
    public Flux<Articulo> findByNombreLikeIgnoreCaseOrderByNombre(String query, int page, int size) {
        return articuloRepository.findByNombreLikeIgnoreCase(query, page,size)
                .map(domainMapper::entityToDomain)
                .doOnComplete(() -> log.info("Consulta de base de datos completada"))
                .doOnError(error -> log.error("Error en consulta de base de datos", error));
    }

    @Override
    public Mono<Articulo> save(Articulo articulo) {
        // Establecer la fecha de creación si es un nuevo artículo
        if (articulo.getIdArticulo() == null) {
            articulo.setFecIngreso(LocalDateTime.now());
        }

        return Mono.just(articulo)
                .map(domainMapper::domainToEntity)
                .flatMap(articuloRepository::save)
                .map(domainMapper::entityToDomain)
                .doOnSuccess(saved -> log.info("Artículo guardado con éxito: {}", saved.getIdArticulo()))
                .doOnError(error -> log.error("Error al guardar artículo", error));
    }
}
