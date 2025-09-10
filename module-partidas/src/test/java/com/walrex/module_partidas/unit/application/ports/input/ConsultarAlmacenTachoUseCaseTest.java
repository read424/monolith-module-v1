package com.walrex.module_partidas.unit.application.ports.input;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.application.ports.output.ConsultarAlmacenTachoPort;
import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.AlmacenTachoResponse;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.domain.service.ConsultarAlmacenTachoService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests unitarios para el caso de uso ConsultarAlmacenTachoUseCase
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarAlmacenTachoUseCase Tests")
class ConsultarAlmacenTachoUseCaseTest {

    @Mock
    private ConsultarAlmacenTachoPort consultarAlmacenTachoPort;

    @InjectMocks
    private ConsultarAlmacenTachoService consultarAlmacenTachoUseCase;

    private ConsultarAlmacenTachoRequest request;
    private AlmacenTachoResponse almacenTachoResponse;

    @BeforeEach
    void setUp() {
        request = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(1)
                .page(0)
                .numRows(10)
                .build();

        List<AlmacenTacho> almacenTachoList = List.of(
                AlmacenTacho.builder()
                        .idOrdeningreso(307874)
                        .idCliente(138)
                        .razonSocial("HUANCATEX S.A.C.")
                        .noAlias("HUANCATEX")
                        .fecRegistro(LocalDateTime.of(2025, 8, 19, 3, 30, 35))
                        .codIngreso("ALGT-I46331")
                        .idDetordeningreso(330449)
                        .idPartida(55509)
                        .codPartida("PA25-0048661")
                        .cntRollos(18)
                        .codReceta("RT25-25388")
                        .noColores("NEGRO")
                        .idTipoTenido(5)
                        .descTenido("DISPERSO")
                        .build(),
                AlmacenTacho.builder()
                        .idOrdeningreso(307797)
                        .idCliente(58)
                        .razonSocial("PADA TEXTIL S.A.C.")
                        .noAlias("PADA")
                        .fecRegistro(LocalDateTime.of(2025, 8, 18, 17, 43, 44))
                        .codIngreso("ALGT-I46323")
                        .idDetordeningreso(330372)
                        .idPartida(55553)
                        .codPartida("PA25-0048701")
                        .cntRollos(10)
                        .build());

        almacenTachoResponse = AlmacenTachoResponse.builder()
                .almacenes(almacenTachoList)
                .totalRecords(2)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Test
    @DisplayName("Debería consultar almacén tacho exitosamente")
    void shouldConsultarAlmacenTachoSuccessfully() {
        // Arrange
        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponse));

        // Act & Assert
        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .expectNextMatches(response -> response.getPartidas().size() == 2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay resultados")
    void shouldReturnEmptyListWhenNoResults() {
        // Arrange
        AlmacenTachoResponse emptyResponse = AlmacenTachoResponse.builder()
                .almacenes(List.of())
                .totalRecords(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(emptyResponse));

        // Act & Assert
        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .expectNextMatches(response -> response.getPartidas().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del puerto de salida")
    void shouldHandlePortError() {
        // Arrange
        RuntimeException error = new RuntimeException("Error en base de datos");
        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.error(error));

        // Act & Assert
        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería procesar request con paginación")
    void shouldProcessRequestWithPagination() {
        // Arrange
        ConsultarAlmacenTachoRequest requestConPaginacion = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(1)
                .page(1)
                .numRows(5)
                .totalPages(3)
                .build();

        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponse));

        // Act & Assert
        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(requestConPaginacion))
                .expectNextMatches(response -> response.getPartidas().size() == 2)
                .verifyComplete();
    }
}
