package com.walrex.module_articulos.domain.service;

import com.walrex.module_articulos.application.ports.output.ArticuloOutputPort;
import com.walrex.module_articulos.domain.model.Articulo;
import com.walrex.module_articulos.domain.model.ArticuloSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ArticuloServiceTest {
    @Mock
    private ArticuloOutputPort articuloOutputPort;

    @InjectMocks
    private ArticuloService articuloService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void debeBuscarArticulosConTerminosSinComodines(){
        //Given
        ArticuloSearchCriteria criteria = ArticuloSearchCriteria.builder()
                .search("CALLTOP")
                .page(0)
                .size(10)
                .build();

        List<Articulo> expectedArticulos = List.of(
                Articulo.builder()
                        .idArticulo(20L)
                        .codArticulo("PQ00006")
                        .descArticulo("CALLTOPREXTO")
                        .build(),
                Articulo.builder()
                        .idArticulo(406L)
                        .codArticulo("PRO00089")
                        .descArticulo("CALLTOPLEX JN")
                        .build()
        );

        when(articuloOutputPort.findByNombreLikeIgnoreCaseOrderByNombre(
                eq("%CALLTOP%"), eq(0), eq(10)))
                .thenReturn(Flux.fromIterable(expectedArticulos));

        // When
        Flux<Articulo> result = articuloService.searchArticulos(criteria);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedArticulos.get(0))
                .expectNext(expectedArticulos.get(1))
                .verifyComplete();

        verify(articuloOutputPort).findByNombreLikeIgnoreCaseOrderByNombre(
                eq("%CALLTOP%"), eq(0), eq(10));
    }

    @Test
    void debeBuscarArticulosConTerminoConComodines(){
        // Given
        ArticuloSearchCriteria criteria = ArticuloSearchCriteria.builder()
                .search("CALLTOP%")
                .page(0)
                .size(10)
                .build();

        List<Articulo> expectedArticulos = List.of(
                Articulo.builder()
                        .idArticulo(20L)
                        .codArticulo("PQ00006")
                        .descArticulo("CALLTOPREXTO")
                        .build(),
                Articulo.builder()
                        .idArticulo(406L)
                        .codArticulo("PRO00089")
                        .descArticulo("CALLTOPLEX JN")
                        .build()
        );

        when(articuloOutputPort.findByNombreLikeIgnoreCaseOrderByNombre(
                eq("CALLTOP%"), eq(0), eq(10)))
                .thenReturn(Flux.fromIterable(expectedArticulos));

        // When
        Flux<Articulo> result = articuloService.searchArticulos(criteria);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedArticulos.get(0))
                .verifyComplete();

        verify(articuloOutputPort).findByNombreLikeIgnoreCaseOrderByNombre(
                eq("CALLTOP%"), eq(0), eq(10));
    }
}
