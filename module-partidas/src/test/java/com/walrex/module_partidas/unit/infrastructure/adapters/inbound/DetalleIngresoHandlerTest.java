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

import com.walrex.module_partidas.application.ports.input.ConsultarDetalleIngresoUseCase;
import com.walrex.module_partidas.domain.model.DetalleIngresoRollos;
import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.dto.ConsultarDetalleIngresoRequest;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.DetalleIngresoHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DetalleIngresoHandler Tests")
class DetalleIngresoHandlerTest {

    @Mock
    private ConsultarDetalleIngresoUseCase consultarDetalleIngresoUseCase;

    @Mock
    private Validator validator;

    @InjectMocks
    private DetalleIngresoHandler detalleIngresoHandler;

    private DetalleIngresoRollos detalleIngreso;

    @BeforeEach
    void setUp() {
        detalleIngreso = DetalleIngresoRollos.builder()
                .idArticulo(1001)
                .codArticulo("ART-001")
                .descArticulo("ARTICULO DE PRUEBA")
                .cntRollos(2)
                .idDetordeningreso(List.of(5001))
                .idOrdeningreso(List.of(10001))
                .rollos(List.of(
                        ItemRollo.builder().codRollo("ROLLO-001").build(),
                        ItemRollo.builder().codRollo("ROLLO-002").build()))
                .build();
    }

    @Test
    @DisplayName("consulta detalle de ingreso exitosamente")
    void shouldConsultarDetalleIngresoSuccessfully() {
        when(validator.validate(any(ConsultarDetalleIngresoRequest.class))).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.just(detalleIngreso));

        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest("45454", "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna not found cuando no hay resultados")
    void shouldReturnNotFoundWhenNoResults() {
        when(validator.validate(any(ConsultarDetalleIngresoRequest.class))).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest("45454", "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna bad request cuando faltan parámetros")
    void shouldReturnBadRequestWhenParametersAreMissing() {
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest(null, "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna bad request cuando falla validación")
    void shouldReturnBadRequestWhenValidationFails() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<ConsultarDetalleIngresoRequest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("idPartida");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("debe ser positivo");
        when(validator.validate(any(ConsultarDetalleIngresoRequest.class))).thenReturn(Set.of(violation));

        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest("-1", "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    @DisplayName("maneja error del caso de uso")
    void shouldHandleUseCaseError() {
        when(validator.validate(any(ConsultarDetalleIngresoRequest.class))).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Error en caso de uso")));

        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest("45454", "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("retorna bad request cuando los parámetros no son numéricos")
    void shouldReturnBadRequestWhenParametersAreNotNumeric() {
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createRequest("abc", "36")))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    private ServerRequest createRequest(String idPartida, String idAlmacen) {
        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.queryParam("id_partida")).thenReturn(Optional.ofNullable(idPartida));
        lenient().when(serverRequest.queryParam("id_almacen")).thenReturn(Optional.ofNullable(idAlmacen));
        return serverRequest;
    }
}
