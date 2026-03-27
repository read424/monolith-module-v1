package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.application.ports.output.SessionArticuloPesajeOutputPort;
import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.SessionPesajeActiva;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import com.walrex.module_almacen.domain.model.exceptions.RolloAsignadoPartidaException;
import com.walrex.module_almacen.domain.model.exceptions.RolloPesajeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PesajeServiceTest {

    @Mock
    private PesajeOutputPort pesajeRepository;

    @Mock
    private PesajeNotificationPort notificationPort;

    @Mock
    private SessionArticuloPesajeOutputPort sessionOutputPort;

    private PesajeService pesajeService;

    @BeforeEach
    void setUp() {
        pesajeService = new PesajeService(pesajeRepository, notificationPort, sessionOutputPort);
    }

    @Test
    void registrarPesaje_Success() {
        PesajeRequest request = new PesajeRequest(10.5);
        PesajeDetalle initialDetalle = PesajeDetalle.builder()
                .idOrdenIngreso(1)
                .cod_rollo("LOTE-1")
                .cnt_registrados(0)
                .id_detordeningreso(100)
                .id_session_hidden(50)
                .lote("LOTE")
                .build();

        PesajeDetalle savedDetalle = PesajeDetalle.builder()
                .id_detordeningresopeso(1)
                .idOrdenIngreso(1)
                .peso_rollo(10.5)
                .cod_rollo("LOTE-1")
                .cnt_registrados(0)
                .id_detordeningreso(100)
                .id_session_hidden(50)
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.just(initialDetalle));
        when(pesajeRepository.saveWeight(any(), anyInt())).thenReturn(Mono.just(savedDetalle));
        when(pesajeRepository.incrementPesoAlmacen(100, 10.5)).thenReturn(Mono.empty());
        when(pesajeRepository.updateSessionState(anyInt())).thenReturn(Mono.just("1"));
        when(notificationPort.notifyWeightRegistered(any())).thenReturn(Mono.empty());

        Mono<PesajeDetalle> result = pesajeService.registrarPesaje(request);

        StepVerifier.create(result)
                .expectNextMatches(detalle ->
                        detalle.getPeso_rollo() == 10.5 &&
                        !detalle.getCompletado() &&
                        detalle.getCnt_registrados() == 1)
                .verifyComplete();

        verify(pesajeRepository).findActiveSessionWithDetail();
        verify(pesajeRepository).saveWeight(any(), eq(100));
        verify(pesajeRepository).incrementPesoAlmacen(100, 10.5);
        verify(pesajeRepository).updateSessionState(50);
        verify(notificationPort).notifyWeightRegistered(any());
    }

    @Test
    void registrarPesaje_CompletionStatus() {
        PesajeRequest request = new PesajeRequest(10.5);
        PesajeDetalle initialDetalle = PesajeDetalle.builder()
                .id_session_hidden(50)
                .cnt_registrados(9)
                .lote("LOTE")
                .id_detordeningreso(100)
                .build();

        PesajeDetalle savedDetalle = PesajeDetalle.builder()
                .id_session_hidden(50)
                .cnt_registrados(9)
                .id_detordeningreso(100)
                .peso_rollo(10.5)
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.just(initialDetalle));
        when(pesajeRepository.saveWeight(any(), any())).thenReturn(Mono.just(savedDetalle));
        when(pesajeRepository.incrementPesoAlmacen(100, 10.5)).thenReturn(Mono.empty());
        when(pesajeRepository.updateSessionState(anyInt())).thenReturn(Mono.just("0"));
        when(notificationPort.notifyWeightRegistered(any())).thenReturn(Mono.empty());

        Mono<PesajeDetalle> result = pesajeService.registrarPesaje(request);

        StepVerifier.create(result)
                .expectNextMatches(detalle -> detalle.getCompletado() && detalle.getCnt_registrados() == 10)
                .verifyComplete();
    }

    @Test
    void obtenerSession_DeactivatesPreviousSession_WhenIdMismatch() {
        Integer requestedId = 200;
        PesajeDetalle activeSession = PesajeDetalle.builder()
                .id_detordeningreso(100)
                .id_session_hidden(50)
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.just(activeSession));
        when(sessionOutputPort.updateSessionStatusToCompleted(50)).thenReturn(Mono.empty());
        when(sessionOutputPort.findStatusByIdDetOrdenIngreso(requestedId)).thenReturn(Mono.empty());

        ArticuloPesajeSession data = ArticuloPesajeSession.builder()
                .id(null)
                .nuRollos(10)
                .totalSaved(java.math.BigDecimal.ZERO)
                .cntRollSaved(0)
                .build();
        when(sessionOutputPort.getArticuloWithSessionDetail(requestedId)).thenReturn(Mono.just(data));
        when(sessionOutputPort.insertSession(anyInt(), anyInt(), anyDouble())).thenReturn(Mono.empty());
        when(sessionOutputPort.findRollosByIdDetOrdenIngreso(requestedId)).thenReturn(reactor.core.publisher.Flux.empty());

        Mono<com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse> result = pesajeService.obtenerSession(requestedId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(sessionOutputPort).updateSessionStatusToCompleted(50);
        verify(sessionOutputPort).insertSession(eq(requestedId), anyInt(), anyDouble());
    }

    @Test
    void obtenerSession_ReactivaSesionCerrada_CuandoFaltanRollos() {
        Integer requestedId = 200;
        SessionPesajeActiva closedSession = SessionPesajeActiva.builder()
                .id(60)
                .idDetOrdenIngreso(requestedId)
                .cntRollos(10)
                .cntRegistro(4)
                .status("0")
                .build();

        ArticuloPesajeSession data = ArticuloPesajeSession.builder()
                .id(60)
                .nuRollos(10)
                .totalSaved(java.math.BigDecimal.valueOf(120.5))
                .cntRollSaved(4)
                .status("1")
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.empty());
        when(sessionOutputPort.findStatusByIdDetOrdenIngreso(requestedId)).thenReturn(Mono.just(closedSession));
        when(sessionOutputPort.updateSessionStatusToActive(60)).thenReturn(Mono.empty());
        when(sessionOutputPort.getArticuloWithSessionDetail(requestedId)).thenReturn(Mono.just(data));
        when(sessionOutputPort.findRollosByIdDetOrdenIngreso(requestedId)).thenReturn(reactor.core.publisher.Flux.empty());

        Mono<com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse> result = pesajeService.obtenerSession(requestedId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getCantidad() == 4 && response.getTotal_kg() == 120.5)
                .verifyComplete();

        verify(sessionOutputPort).updateSessionStatusToActive(60);
    }

    @Test
    void deleteGuideRoll_ReturnsError_WhenRolloDoesNotExist() {
        Integer rolloId = 999;
        when(pesajeRepository.existsRolloById(rolloId)).thenReturn(Mono.just(false));

        StepVerifier.create(pesajeService.deleteGuideRoll(rolloId))
                .expectErrorMatches(error -> error instanceof RolloPesajeNotFoundException
                        && error.getMessage().contains(String.valueOf(rolloId)))
                .verify();

        verify(pesajeRepository, never()).findAssignedPartidaCode(anyInt());
        verify(pesajeRepository, never()).deleteRolloById(anyInt());
    }

    @Test
    void deleteGuideRoll_ReturnsConflict_WhenRolloIsAssignedToPartida() {
        Integer rolloId = 10;
        when(pesajeRepository.existsRolloById(rolloId)).thenReturn(Mono.just(true));
        when(pesajeRepository.findAssignedPartidaCode(rolloId)).thenReturn(Mono.just("P-001-R1"));

        StepVerifier.create(pesajeService.deleteGuideRoll(rolloId))
                .expectErrorMatches(error -> error instanceof RolloAsignadoPartidaException
                        && error.getMessage().contains("P-001-R1"))
                .verify();

        verify(pesajeRepository, never()).deleteRolloById(anyInt());
    }

    @Test
    void deleteGuideRoll_Completes_WhenRolloExistsAndIsNotAssigned() {
        Integer rolloId = 11;
        when(pesajeRepository.existsRolloById(rolloId)).thenReturn(Mono.just(true));
        when(pesajeRepository.findAssignedPartidaCode(rolloId)).thenReturn(Mono.empty());
        when(pesajeRepository.deleteRolloById(rolloId)).thenReturn(Mono.empty());

        StepVerifier.create(pesajeService.deleteGuideRoll(rolloId))
                .verifyComplete();

        verify(pesajeRepository).deleteRolloById(rolloId);
    }
}
