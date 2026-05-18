package com.walrex.module_partidas.unit.infrastructure.adapters.inbound;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.walrex.module_partidas.application.ports.input.ConsultarAlmacenTachoUseCase;
import com.walrex.module_partidas.domain.model.dto.AlmacenTachoResponseDTO;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.domain.model.dto.PartidaTachoResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.AlmacenTachoHandler;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.AlmacenTachoResponseMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.ListPartidaTachoResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlmacenTachoHandler Tests")
class AlmacenTachoHandlerTest {

    @Mock
    private ConsultarAlmacenTachoUseCase consultarAlmacenTachoUseCase;

    @Mock
    private AlmacenTachoResponseMapper almacenTachoResponseMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private AlmacenTachoHandler almacenTachoHandler;

    private ConsultarAlmacenTachoRequest request;
    private AlmacenTachoResponseDTO almacenTachoResponseDTO;
    private ListPartidaTachoResponse httpResponse;

    @BeforeEach
    void setUp() {
        request = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(36)
                .page(0)
                .numRows(10)
                .codPartida("")
                .build();

        almacenTachoResponseDTO = AlmacenTachoResponseDTO.builder()
                .partidas(List.of(PartidaTachoResponse.builder()
                        .idPartida(55509)
                        .codPartida("PA25-0048661")
                        .timeElapsed(123L)
                        .build()))
                .totalRecords(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        httpResponse = ListPartidaTachoResponse.builder()
                .partidas(List.of())
                .totalRecords(1)
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
        when(validator.validate(any(ConsultarAlmacenTachoRequest.class))).thenReturn(Set.of());
        when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponseDTO));
        when(almacenTachoResponseMapper.toListPartidaTachoResponse(almacenTachoResponseDTO))
                .thenReturn(httpResponse);

        StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createRequest("36", "0", "10", "")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna bad request cuando falla validación")
    void shouldReturnBadRequestWhenValidationFails() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<ConsultarAlmacenTachoRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("es obligatorio");
        when(validator.validate(any(ConsultarAlmacenTachoRequest.class))).thenReturn(Set.of(violation));

        StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createRequest(null, "0", "10", "")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    @DisplayName("maneja error del caso de uso")
    void shouldHandleUseCaseError() {
        when(validator.validate(any(ConsultarAlmacenTachoRequest.class))).thenReturn(Set.of());
        when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Error en caso de uso")));

        StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createRequest("36", "0", "10", "")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("usa valores por defecto para paginación")
    void shouldUseDefaultPaginationValues() {
        when(validator.validate(any(ConsultarAlmacenTachoRequest.class))).thenReturn(Set.of());
        when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponseDTO));
        when(almacenTachoResponseMapper.toListPartidaTachoResponse(almacenTachoResponseDTO))
                .thenReturn(httpResponse);

        StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createRequest("36", null, null, null)))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(consultarAlmacenTachoUseCase).listarPartidasInTacho(argThat(actual ->
                Integer.valueOf(36).equals(actual.getIdAlmacen())
                        && Integer.valueOf(0).equals(actual.getPage())
                        && Integer.valueOf(10).equals(actual.getNumRows())
                        && "".equals(actual.getCodPartida())));
    }

    @Test
    @DisplayName("propaga codPartida cuando viene informado")
    void shouldPropagateCodPartida() {
        when(validator.validate(any(ConsultarAlmacenTachoRequest.class))).thenReturn(Set.of());
        when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                .thenReturn(Mono.just(almacenTachoResponseDTO));
        when(almacenTachoResponseMapper.toListPartidaTachoResponse(almacenTachoResponseDTO))
                .thenReturn(httpResponse);

        StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createRequest("36", "1", "5", "PA25")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(consultarAlmacenTachoUseCase).listarPartidasInTacho(argThat(actual ->
                "PA25".equals(actual.getCodPartida())
                        && Integer.valueOf(1).equals(actual.getPage())
                        && Integer.valueOf(5).equals(actual.getNumRows())));
    }

    private ServerRequest createRequest(String idAlmacen, String page, String numRows, String codPartida) {
        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.queryParam("id_almacen")).thenReturn(Optional.ofNullable(idAlmacen));
        when(serverRequest.queryParam("page")).thenReturn(Optional.ofNullable(page));
        when(serverRequest.queryParam("num_rows")).thenReturn(Optional.ofNullable(numRows));
        when(serverRequest.queryParam("cod_partida")).thenReturn(Optional.ofNullable(codPartida));
        return serverRequest;
    }
}
