package com.walrex.module_partidas.application.service;

import static org.mockito.Mockito.*;

import com.walrex.module_partidas.domain.service.ConsultarProcesosPartidaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.application.ports.output.ConsultarProcesosPartidaPort;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.domain.model.dto.ConsultarProcesosPartidaRequest;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Test unitario para ConsultarProcesosPartidaService
 * Sigue TDD y utiliza StepVerifier para testing reactivo
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)
class ConsultarProcesosPartidaServiceTest {

    @Mock
    private ConsultarProcesosPartidaPort consultarProcesosPartidaPort;

    @InjectMocks
    private ConsultarProcesosPartidaService service;

    private ConsultarProcesosPartidaRequest request;
    private ProcesoPartida procesoPartida;

    @BeforeEach
    void setUp() {
        request = ConsultarProcesosPartidaRequest.builder()
                .idPartida(123)
                .build();

        procesoPartida = ProcesoPartida.builder()
                .idCliente(1)
                .idPartida(123)
                .idPartidaMaquina(456)
                .idRuta(789)
                .idArticulo(101)
                .idProceso(202)
                .idDetRuta(303)
                .noProceso("PROCESO_001")
                .idAlmacen(404)
                .idMaquina(505)
                .idTipoMaquina(606)
                .iniciado(false)
                .finalizado(false)
                .isPendiente(true)
                .status(0)
                .isMainProceso(true)
                .descMaq("Máquina Principal")
                .build();
    }

    @Test
    @DisplayName("Debería consultar procesos de partida exitosamente")
    void deberiaConsultarProcesosPartidaExitosamente() {
        // Given
        when(consultarProcesosPartidaPort.consultarProcesosPartida(123))
                .thenReturn(Flux.just(procesoPartida));

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .expectNext(procesoPartida)
                .verifyComplete();

        verify(consultarProcesosPartidaPort).consultarProcesosPartida(123);
    }

    @Test
    @DisplayName("Debería retornar múltiples procesos de partida")
    void deberiaRetornarMultiplesProcesosPartida() {
        // Given
        ProcesoPartida proceso2 = ProcesoPartida.builder()
                .idPartida(123)
                .noProceso("PROCESO_002")
                .isPendiente(false)
                .build();

        when(consultarProcesosPartidaPort.consultarProcesosPartida(123))
                .thenReturn(Flux.just(procesoPartida, proceso2));

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .expectNext(procesoPartida)
                .expectNext(proceso2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar error cuando ID de partida es null")
    void deberiaRetornarErrorCuandoIdPartidaEsNull() {
        // Given
        request.setIdPartida(null);

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(consultarProcesosPartidaPort);
    }

    @Test
    @DisplayName("Debería retornar error cuando ID de partida es menor o igual a 0")
    void deberiaRetornarErrorCuandoIdPartidaEsInvalido() {
        // Given
        request.setIdPartida(0);

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(consultarProcesosPartidaPort);
    }

    @Test
    @DisplayName("Debería propagar error del puerto de salida")
    void deberiaPropagarErrorDelPuertoDeSalida() {
        // Given
        RuntimeException error = new RuntimeException("Error de base de datos");
        when(consultarProcesosPartidaPort.consultarProcesosPartida(123))
                .thenReturn(Flux.error(error));

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .expectError(RuntimeException.class)
                .verify();

        verify(consultarProcesosPartidaPort).consultarProcesosPartida(123);
    }

    @Test
    @DisplayName("Debería retornar Flux vacío cuando no hay procesos")
    void deberiaRetornarFluxVacioCuandoNoHayProcesos() {
        // Given
        when(consultarProcesosPartidaPort.consultarProcesosPartida(123))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(service.consultarProcesosPartida(request))
                .verifyComplete();

        verify(consultarProcesosPartidaPort).consultarProcesosPartida(123);
    }
}
