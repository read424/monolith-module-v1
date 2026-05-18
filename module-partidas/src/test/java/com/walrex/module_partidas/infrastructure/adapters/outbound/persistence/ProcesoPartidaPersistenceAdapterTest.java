package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.ProcesoPartidaMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.ProcesoPartidaProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.ProcesoPartidaRepository;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProcesoPartidaPersistenceAdapterTest {

    @Mock
    private ProcesoPartidaRepository repository;

    @Mock
    private ProcesoPartidaMapper mapper;

    private ProcesoPartidaPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProcesoPartidaPersistenceAdapter(repository, mapper);
    }

    @Test
    @DisplayName("Debería crear adapter con dependencias inyectadas")
    void deberiaCrearAdapterConDependenciasInyectadas() {
        assertThat(adapter).isNotNull();
        assertThat(repository).isNotNull();
        assertThat(mapper).isNotNull();
    }

    @Test
    @DisplayName("Debería consultar procesos de partida usando el adapter")
    void deberiaConsultarProcesosPartidaUsandoAdapter() {
        Integer idPartida = 1;
        ProcesoPartidaProjection projection = ProcesoPartidaProjection.builder()
                .idPartida(idPartida)
                .noProceso("Tejido")
                .isPendiente(true)
                .build();
        ProcesoPartida proceso = ProcesoPartida.builder()
                .idPartida(idPartida)
                .noProceso("Tejido")
                .isPendiente(true)
                .build();

        when(repository.findProcesosByPartida(idPartida)).thenReturn(Flux.just(projection));
        when(mapper.toDomain(projection)).thenReturn(proceso);

        StepVerifier.create(adapter.consultarProcesosPartida(idPartida))
                .expectNext(proceso)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar consulta con ID de partida inválido")
    void deberiaManejarConsultaConIdPartidaInvalido() {
        Integer idPartida = -1;
        when(repository.findProcesosByPartida(idPartida)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.consultarProcesosPartida(idPartida))
                .verifyComplete();
    }
}
