package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.ProcesoPartidaMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.ProcesoPartidaRepository;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Test de integración para ProcesoPartidaPersistenceAdapter
 * Utiliza @DataR2dbcTest para testing de persistencia reactiva
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@DataR2dbcTest
@ActiveProfiles("test")
class ProcesoPartidaPersistenceAdapterTest {

    @Autowired
    private ProcesoPartidaRepository repository;

    @Autowired
    private ProcesoPartidaMapper mapper;

    private ProcesoPartidaPersistenceAdapter adapter;

    @Test
    @DisplayName("Debería crear adapter con dependencias inyectadas")
    void deberiaCrearAdapterConDependenciasInyectadas() {
        // Given & When
        adapter = new ProcesoPartidaPersistenceAdapter(repository, mapper);

        // Then
        assertThat(adapter).isNotNull();
        assertThat(repository).isNotNull();
        assertThat(mapper).isNotNull();
    }

    @Test
    @DisplayName("Debería consultar procesos de partida usando el adapter")
    void deberiaConsultarProcesosPartidaUsandoAdapter() {
        // Given
        adapter = new ProcesoPartidaPersistenceAdapter(repository, mapper);
        Integer idPartida = 1; // Asumiendo que existe en la base de datos de test

        // When & Then
        Flux<ProcesoPartida> resultado = adapter.consultarProcesosPartida(idPartida);

        StepVerifier.create(resultado)
                .expectNextCount(0) // Puede que no haya datos en test, pero no debe fallar
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar consulta con ID de partida inválido")
    void deberiaManejarConsultaConIdPartidaInvalido() {
        // Given
        adapter = new ProcesoPartidaPersistenceAdapter(repository, mapper);
        Integer idPartida = -1; // ID inválido

        // When & Then
        Flux<ProcesoPartida> resultado = adapter.consultarProcesosPartida(idPartida);

        StepVerifier.create(resultado)
                .expectNextCount(0) // No debe fallar, solo retornar vacío
                .verifyComplete();
    }
}
