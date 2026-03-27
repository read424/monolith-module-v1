package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.DeleteGuideRollUseCase;
import com.walrex.module_almacen.application.ports.input.ObtenerSessionArticuloPesajeUseCase;
import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.domain.model.exceptions.RolloAsignadoPartidaException;
import com.walrex.module_almacen.domain.model.exceptions.RolloPesajeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PesajeHandlerTest {

    @Mock
    private PesajeUseCase pesajeUseCase;

    @Mock
    private ObtenerSessionArticuloPesajeUseCase obtenerSessionUseCase;

    @Mock
    private DeleteGuideRollUseCase deleteGuideRollUseCase;

    private PesajeHandler pesajeHandler;

    @BeforeEach
    void setUp() {
        pesajeHandler = new PesajeHandler(pesajeUseCase, obtenerSessionUseCase, deleteGuideRollUseCase);
    }

    @Test
    void deleteGuideRoll_ReturnsNoContent_WhenDeleteSucceeds() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetordenIngresoRollo", "10")
                .build();
        when(deleteGuideRollUseCase.deleteGuideRoll(10)).thenReturn(Mono.empty());

        Mono<ServerResponse> response = pesajeHandler.deleteGuideRoll(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    void deleteGuideRoll_ReturnsNotFound_WhenRolloDoesNotExist() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetordenIngresoRollo", "11")
                .build();
        when(deleteGuideRollUseCase.deleteGuideRoll(11))
                .thenReturn(Mono.error(new RolloPesajeNotFoundException("No existe")));

        Mono<ServerResponse> response = pesajeHandler.deleteGuideRoll(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.NOT_FOUND
                        && MediaType.APPLICATION_JSON.equals(serverResponse.headers().getContentType()))
                .verifyComplete();
    }

    @Test
    void deleteGuideRoll_ReturnsConflict_WhenRolloIsAssignedToPartida() {
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetordenIngresoRollo", "12")
                .build();
        when(deleteGuideRollUseCase.deleteGuideRoll(12))
                .thenReturn(Mono.error(new RolloAsignadoPartidaException("Asignado a partida")));

        Mono<ServerResponse> response = pesajeHandler.deleteGuideRoll(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.CONFLICT
                        && MediaType.APPLICATION_JSON.equals(serverResponse.headers().getContentType()))
                .verifyComplete();
    }
}
