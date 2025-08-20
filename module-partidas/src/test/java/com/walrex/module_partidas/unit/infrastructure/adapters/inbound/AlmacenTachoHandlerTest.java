package com.walrex.module_partidas.unit.infrastructure.adapters.inbound;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.walrex.module_partidas.application.ports.input.ConsultarAlmacenTachoUseCase;
import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.router.AlmacenTachoHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests unitarios para el handler reactivo AlmacenTachoHandler
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlmacenTachoHandler Tests")
class AlmacenTachoHandlerTest {

        @Mock
        private ConsultarAlmacenTachoUseCase consultarAlmacenTachoUseCase;

        @Mock
        private Validator validator;

        @InjectMocks
        private AlmacenTachoHandler almacenTachoHandler;

        private ConsultarAlmacenTachoRequest request;
        private List<AlmacenTacho> almacenTachoList;

        @BeforeEach
        void setUp() {
                request = ConsultarAlmacenTachoRequest.builder()
                                .idAlmacen(36)
                                .page(0)
                                .numRows(10)
                                .build();

                almacenTachoList = List.of(
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
                                                .build());
        }

        @Test
        @DisplayName("Debería consultar almacén tacho exitosamente")
        void shouldConsultarAlmacenTachoSuccessfully() {
                // Arrange
                when(validator.validate(request)).thenReturn(Set.of());
                when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                                .thenReturn(Flux.fromIterable(almacenTachoList));

                // Act & Assert
                StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createMockServerRequest(request)))
                                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay resultados")
        void shouldReturnEmptyListWhenNoResults() {
                // Arrange
                when(validator.validate(request)).thenReturn(Set.of());
                when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                                .thenReturn(Flux.empty());

                // Act & Assert
                StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createMockServerRequest(request)))
                                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería manejar error del caso de uso")
        void shouldHandleUseCaseError() {
                // Arrange
                when(validator.validate(request)).thenReturn(Set.of());
                RuntimeException error = new RuntimeException("Error en caso de uso");
                when(consultarAlmacenTachoUseCase.listarPartidasInTacho(any(ConsultarAlmacenTachoRequest.class)))
                                .thenReturn(Flux.error(error));

                // Act & Assert
                StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(createMockServerRequest(request)))
                                .expectNextMatches(
                                                response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería validar request con idAlmacen obligatorio")
        void shouldValidateRequestWithRequiredIdAlmacen() {
                // Arrange
                ConsultarAlmacenTachoRequest requestSinIdAlmacen = ConsultarAlmacenTachoRequest.builder()
                                .page(0)
                                .numRows(10)
                                .build();

                // Configurar validator mock para request inválido
                when(validator.validate(requestSinIdAlmacen)).thenReturn(Set.of(
                                mock(ConstraintViolation.class)));

                // Act & Assert
                StepVerifier.create(
                                almacenTachoHandler.consultarAlmacenTacho(createMockServerRequest(requestSinIdAlmacen)))
                                .expectNextMatches(
                                                response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería manejar error de validación con IllegalArgumentException")
        void shouldHandleValidationErrorWithIllegalArgumentException() {
                // Arrange
                ConsultarAlmacenTachoRequest requestInvalido = ConsultarAlmacenTachoRequest.builder()
                                .page(-1) // Página negativa inválida
                                .numRows(0) // Número de filas inválido
                                .build();

                // Configurar validator mock para request inválido
                when(validator.validate(requestInvalido)).thenReturn(Set.of(
                                mock(ConstraintViolation.class)));

                // Act & Assert
                StepVerifier.create(
                                almacenTachoHandler.consultarAlmacenTacho(createMockServerRequest(requestInvalido)))
                                .expectNextMatches(
                                                response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería manejar error cuando el body del request es inválido")
        void shouldHandleInvalidRequestBodyError() {
                // Arrange
                ServerRequest mockRequest = mock(ServerRequest.class);
                when(mockRequest.bodyToMono(ConsultarAlmacenTachoRequest.class))
                                .thenReturn(Mono.error(new IllegalArgumentException("Body inválido")));

                // Act & Assert
                StepVerifier.create(almacenTachoHandler.consultarAlmacenTacho(mockRequest))
                                .expectNextMatches(
                                                response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Debería manejar múltiples errores de validación")
        void shouldHandleMultipleValidationErrors() {
                // Arrange
                ConsultarAlmacenTachoRequest requestConMultiplesErrores = ConsultarAlmacenTachoRequest.builder()
                                .page(-1)
                                .numRows(0)
                                .build();

                // Configurar validator mock para múltiples errores de validación
                ConstraintViolation<ConsultarAlmacenTachoRequest> violation1 = mock(ConstraintViolation.class);
                ConstraintViolation<ConsultarAlmacenTachoRequest> violation2 = mock(ConstraintViolation.class);
                when(validator.validate(requestConMultiplesErrores)).thenReturn(Set.of(violation1, violation2));

                // Act & Assert
                StepVerifier.create(
                                almacenTachoHandler.consultarAlmacenTacho(
                                                createMockServerRequest(requestConMultiplesErrores)))
                                .expectNextMatches(
                                                response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                .verifyComplete();
        }

        /**
         * Crea un ServerRequest mock para testing
         */
        private ServerRequest createMockServerRequest(ConsultarAlmacenTachoRequest request) {
                // Mock del ServerRequest con bodyToMono configurado
                ServerRequest mockRequest = mock(ServerRequest.class);
                when(mockRequest.bodyToMono(ConsultarAlmacenTachoRequest.class))
                                .thenReturn(Mono.just(request));
                return mockRequest;
        }
}
