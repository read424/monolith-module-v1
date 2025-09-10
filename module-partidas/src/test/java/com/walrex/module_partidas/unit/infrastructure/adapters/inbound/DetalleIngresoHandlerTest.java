package com.walrex.module_partidas.unit.infrastructure.adapters.inbound;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.*;
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
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests unitarios para el handler reactivo DetalleIngresoHandler
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("DetalleIngresoHandler Tests")
class DetalleIngresoHandlerTest {

    @Mock
    private ConsultarDetalleIngresoUseCase consultarDetalleIngresoUseCase;

    @Mock
    private Validator validator;

    @InjectMocks
    private DetalleIngresoHandler detalleIngresoHandler;

    private ConsultarDetalleIngresoRequest request;
    private List<DetalleIngresoRollos> detalleIngresoList;
    private List<ItemRollo> rollosList;

    @BeforeEach
    void setUp() {
        request = ConsultarDetalleIngresoRequest.builder()
                .idPartida(45454)
                .idAlmacen(36)
                .build();

        // Crear rollos de ejemplo
        rollosList = List.of(
                ItemRollo.builder()
                        .codRollo("ROLLO-001")
                        .despacho(false)
                        .disabled("false")
                        .idAlmacen(36)
                        .idDetPartida(12345)
                        .idIngresoAlmacen(67890)
                        .idIngresopeso(11111)
                        .idOrdeningreso(22222)
                        .idRolloIngreso(33333)
                        .isParentRollo(1)
                        .noAlmacen("ALMACEN TACHO")
                        .numChildRoll(0)
                        .pesoAcabado(150.5)
                        .pesoRollo(150.5)
                        .pesoSaldo(150.5)
                        .pesoSalida(0.0)
                        .status(1)
                        .build(),
                ItemRollo.builder()
                        .codRollo("ROLLO-002")
                        .despacho(true)
                        .disabled("false")
                        .idAlmacen(36)
                        .idDetPartida(12346)
                        .idIngresoAlmacen(67891)
                        .idIngresopeso(11112)
                        .idOrdeningreso(22223)
                        .idRolloIngreso(33334)
                        .isParentRollo(0)
                        .noAlmacen("ALMACEN TACHO")
                        .numChildRoll(0)
                        .pesoAcabado(200.0)
                        .pesoRollo(200.0)
                        .pesoSaldo(50.0)
                        .pesoSalida(150.0)
                        .status(1)
                        .build());

        // Crear detalle de ingreso con rollos
        detalleIngresoList = List.of(
                DetalleIngresoRollos.builder()
                        .abrevUnidad("KG")
                        .cntRollos(2)
                        .codArticulo("ART-001")
                        .descArticulo("ARTÍCULO DE PRUEBA")
                        .idArticulo(1001)
                        .idDetordeningreso(List.of(5001))
                        .idOrdeningreso(List.of(10001))
                        .idTipoProducto(1)
                        .idUnidad(1)
                        .rollos(rollosList)
                        .build());
    }

    @Test
    @DisplayName("Debería consultar detalle de ingreso exitosamente")
    void shouldConsultarDetalleIngresoSuccessfully() {
        // Arrange
        when(validator.validate(request)).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.just(detalleIngresoList.get(0)));

        // Act & Assert
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(request)))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay resultados")
    void shouldReturnEmptyListWhenNoResults() {
        // Arrange
        when(validator.validate(request)).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(request)))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso")
    void shouldHandleUseCaseError() {
        // Arrange
        when(validator.validate(request)).thenReturn(Set.of());
        RuntimeException error = new RuntimeException("Error en caso de uso");
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.error(error));

        // Act & Assert
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(request)))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería validar request con idPartida obligatorio")
    void shouldValidateRequestWithRequiredIdPartida() {
        // Arrange
        ConsultarDetalleIngresoRequest requestSinIdPartida = ConsultarDetalleIngresoRequest.builder()
                .idAlmacen(36)
                .build();

        // Configurar validator mock para request inválido
        when(validator.validate(requestSinIdPartida)).thenReturn(Set.of(
                mock(ConstraintViolation.class)));

        // Act & Assert
        StepVerifier.create(
                detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(requestSinIdPartida)))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería validar request con idAlmacen obligatorio")
    void shouldValidateRequestWithRequiredIdAlmacen() {
        // Arrange
        ConsultarDetalleIngresoRequest requestSinIdAlmacen = ConsultarDetalleIngresoRequest.builder()
                .idPartida(45454)
                .build();

        // Configurar validator mock para request inválido
        when(validator.validate(requestSinIdAlmacen)).thenReturn(Set.of(
                mock(ConstraintViolation.class)));

        // Act & Assert
        StepVerifier.create(
                detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(requestSinIdAlmacen)))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error de validación con IllegalArgumentException")
    void shouldHandleValidationErrorWithIllegalArgumentException() {
        // Arrange
        ConsultarDetalleIngresoRequest requestInvalido = ConsultarDetalleIngresoRequest.builder()
                .idPartida(-1) // ID de partida inválido
                .idAlmacen(0) // ID de almacén inválido
                .build();

        // Configurar validator mock para request inválido
        when(validator.validate(requestInvalido)).thenReturn(Set.of(
                mock(ConstraintViolation.class)));

        // Act & Assert
        StepVerifier.create(
                detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(requestInvalido)))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error cuando el body del request es inválido")
    void shouldHandleInvalidRequestBodyError() {
        // Arrange
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.bodyToMono(ConsultarDetalleIngresoRequest.class))
                .thenReturn(Mono.error(new IllegalArgumentException("Body inválido")));

        // Act & Assert
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(mockRequest))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar múltiples errores de validación")
    void shouldHandleMultipleValidationErrors() {
        // Arrange
        ConsultarDetalleIngresoRequest requestConMultiplesErrores = ConsultarDetalleIngresoRequest.builder()
                .idPartida(-1)
                .idAlmacen(0)
                .build();

        // Configurar validator mock para múltiples errores de validación
        ConstraintViolation<ConsultarDetalleIngresoRequest> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<ConsultarDetalleIngresoRequest> violation2 = mock(ConstraintViolation.class);
        when(validator.validate(requestConMultiplesErrores)).thenReturn(Set.of(violation1, violation2));

        // Act & Assert
        StepVerifier.create(
                detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(requestConMultiplesErrores)))
                .expectNextMatches(
                        response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería procesar detalle de ingreso con múltiples rollos")
    void shouldProcessDetalleIngresoWithMultipleRollos() {
        // Arrange
        when(validator.validate(request)).thenReturn(Set.of());
        when(consultarDetalleIngresoUseCase.consultarDetalleIngreso(any(ConsultarDetalleIngresoRequest.class)))
                .thenReturn(Mono.just(detalleIngresoList.get(0)));

        // Act & Assert
        StepVerifier.create(detalleIngresoHandler.consultarDetalleIngreso(createMockServerRequest(request)))
                .expectNextMatches(response -> {
                    // Verificar que la respuesta sea exitosa
                    boolean isOk = response.statusCode() == HttpStatus.OK;
                    log.info("Respuesta del handler: {}", response.statusCode());
                    return isOk;
                })
                .verifyComplete();
    }

    /**
     * Crea un ServerRequest mock para testing
     */
    private ServerRequest createMockServerRequest(ConsultarDetalleIngresoRequest request) {
        // Mock del ServerRequest con bodyToMono configurado
        ServerRequest mockRequest = mock(ServerRequest.class);
        when(mockRequest.bodyToMono(ConsultarDetalleIngresoRequest.class))
                .thenReturn(Mono.just(request));
        return mockRequest;
    }
}
