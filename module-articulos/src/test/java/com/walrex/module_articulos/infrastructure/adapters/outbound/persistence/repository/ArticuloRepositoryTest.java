package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataR2dbcTest
public class ArticuloRepositoryTest {
    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    @BeforeEach
    void setup(){
        template.delete(ArticuloEntity.class).all()
                .then()
                .thenMany(template.insert(createTestArticulos()))
                .blockLast();
    }

    private Flux<ArticuloEntity> createTestArticulos() {
        return Flux.just(
                ArticuloEntity.builder()
                        .cod_articulo("PRO00105")
                        .desc_articulo("CALLTOZYME")
                        .descripcion("Descripción 1")
                        .build(),
                ArticuloEntity.builder()
                        .cod_articulo("PQ00006")
                        .desc_articulo("CALLTOPREXTO")
                        .descripcion("Descripción 2")
                        .build(),
                ArticuloEntity.builder()
                        .cod_articulo("PRO00089")
                        .desc_articulo("CALLTOPLEX JN")
                        .descripcion("Descripción 3")
                        .build()
        );
    }

    @Test
    void debeBuscarPorNombreLike() {
        // When
        Flux<ArticuloEntity> result = articuloRepository
                .findByNombreLikeIgnoreCase("%CALLTOP%", 10, 0);

        // Then
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

}
