package com.walrex.module_partidas.unit.application.ports.input;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.application.ports.output.ConsultarAlmacenTachoPort;
import com.walrex.module_partidas.domain.mapper.AlmacenTachoResponseDTOMapper;
import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.AlmacenTachoResponse;
import com.walrex.module_partidas.domain.model.dto.AlmacenTachoResponseDTO;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.domain.model.dto.PartidaTachoResponse;
import com.walrex.module_partidas.domain.service.ConsultarAlmacenTachoService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarAlmacenTachoUseCase Tests")
class ConsultarAlmacenTachoUseCaseTest {

    @Mock
    private ConsultarAlmacenTachoPort consultarAlmacenTachoPort;

    @Mock
    private AlmacenTachoResponseDTOMapper almacenTachoResponseMapper;

    @InjectMocks
    private ConsultarAlmacenTachoService consultarAlmacenTachoUseCase;

    private ConsultarAlmacenTachoRequest request;
    private AlmacenTachoResponse almacenTachoResponse;
    private AlmacenTachoResponseDTO almacenTachoResponseDTO;

    @BeforeEach
    void setUp() {
        request = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(1)
                .page(0)
                .numRows(10)
                .build();

        almacenTachoResponse = AlmacenTachoResponse.builder()
                .almacenes(List.of(
                        AlmacenTacho.builder()
                                .idPartida(55509)
                                .codPartida("PA25-0048661")
                                .fecRegistro(LocalDateTime.now().minusMinutes(5))
                                .build(),
                        AlmacenTacho.builder()
                                .idPartida(55553)
                                .codPartida("PA25-0048701")
                                .fecRegistro(LocalDateTime.now().minusMinutes(10))
                                .build()))
                .totalRecords(2)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        almacenTachoResponseDTO = AlmacenTachoResponseDTO.builder()
                .partidas(List.of(
                        PartidaTachoResponse.builder().idPartida(55509).codPartida("PA25-0048661").build(),
                        PartidaTachoResponse.builder().idPartida(55553).codPartida("PA25-0048701").build()))
                .totalRecords(2)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Test
    @DisplayName("consulta almacén tacho exitosamente")
    void shouldConsultarAlmacenTachoSuccessfully() {
        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponse));
        when(almacenTachoResponseMapper.toDTO(almacenTachoResponse))
                .thenReturn(almacenTachoResponseDTO);

        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(2, response.getPartidas().size()))
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna lista vacía cuando no hay resultados")
    void shouldReturnEmptyListWhenNoResults() {
        AlmacenTachoResponse emptyResponse = AlmacenTachoResponse.builder()
                .almacenes(List.of())
                .totalRecords(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        AlmacenTachoResponseDTO emptyDto = AlmacenTachoResponseDTO.builder()
                .partidas(List.of())
                .totalRecords(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(emptyResponse));
        when(almacenTachoResponseMapper.toDTO(emptyResponse)).thenReturn(emptyDto);

        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertTrue(response.getPartidas().isEmpty()))
                .verifyComplete();
    }

    @Test
    @DisplayName("maneja error del puerto de salida")
    void shouldHandlePortError() {
        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Error en base de datos")));

        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("procesa request con paginación")
    void shouldProcessRequestWithPagination() {
        ConsultarAlmacenTachoRequest requestConPaginacion = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(1)
                .page(1)
                .numRows(5)
                .totalPages(3)
                .build();

        when(consultarAlmacenTachoPort.consultarAlmacenTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponse));
        when(almacenTachoResponseMapper.toDTO(almacenTachoResponse))
                .thenReturn(almacenTachoResponseDTO);

        StepVerifier.create(consultarAlmacenTachoUseCase.listarPartidasInTacho(requestConPaginacion))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(2, response.getPartidas().size()))
                .verifyComplete();
    }
}
