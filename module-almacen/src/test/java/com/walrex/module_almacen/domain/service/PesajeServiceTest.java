package com.walrex.module_almacen.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.application.ports.output.SessionArticuloPesajeOutputPort;
import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        // Given
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

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.of(initialDetalle));
        when(pesajeRepository.saveWeight(any(), anyInt())).thenReturn(Mono.of(savedDetalle));
        when(pesajeRepository.updateSessionState(anyInt())).thenReturn(Mono.of("1")); // status = '1' (not completed)
        when(notificationPort.notifyWeightRegistered(any())).thenReturn(Mono.empty());

        // When
        Mono<PesajeDetalle> result = pesajeService.registrarPesaje(request);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(detalle -> 
                        detalle.getPeso_rollo() == 10.5 && 
                        !detalle.getCompletado() && 
                        detalle.getCnt_registrados() == 1)
                .verifyComplete();

        verify(pesajeRepository).findActiveSessionWithDetail();
        verify(pesajeRepository).saveWeight(any(), eq(100));
        verify(pesajeRepository).updateSessionState(50);
        verify(notificationPort).notifyWeightRegistered(any());
    }

    @Test
    void registrarPesaje_CompletionStatus() {
        // Given
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
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.of(initialDetalle));
        when(pesajeRepository.saveWeight(any(), any())).thenReturn(Mono.of(savedDetalle));
        when(pesajeRepository.updateSessionState(anyInt())).thenReturn(Mono.of("0")); // status = '0' (completed)
        when(notificationPort.notifyWeightRegistered(any())).thenReturn(Mono.empty());

        // When
        Mono<PesajeDetalle> result = pesajeService.registrarPesaje(request);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(detalle -> detalle.getCompletado() && detalle.getCnt_registrados() == 10)
                .verifyComplete();
    }

    @Test
    void obtenerSession_DeactivatesPreviousSession_WhenIdMismatch() {
        // Given
        Integer requestedId = 200;
        PesajeDetalle activeSession = PesajeDetalle.builder()
                .id_detordeningreso(100)
                .id_session_hidden(50)
                .build();

        when(pesajeRepository.findActiveSessionWithDetail()).thenReturn(Mono.of(activeSession));
        when(sessionOutputPort.updateSessionStatusToCompleted(50)).thenReturn(Mono.empty());
        when(sessionOutputPort.findStatusByIdDetOrdenIngreso(requestedId)).thenReturn(Mono.empty());
        
        ArticuloPesajeSession data = ArticuloPesajeSession.builder()
                .id(null)
                .nuRollos(10)
                .totalSaved(java.math.BigDecimal.ZERO)
                .cntRollSaved(0)
                .build();
        when(sessionOutputPort.getArticuloWithSessionDetail(requestedId)).thenReturn(Mono.of(data));
        when(sessionOutputPort.insertSession(anyInt(), anyInt(), anyDouble())).thenReturn(Mono.empty());
        when(sessionOutputPort.findRollosByIdDetOrdenIngreso(requestedId)).thenReturn(reactor.core.publisher.Flux.empty());

        // When
        Mono<com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse> result = pesajeService.obtenerSession(requestedId);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(sessionOutputPort).updateSessionStatusToCompleted(50);
        verify(sessionOutputPort).insertSession(eq(requestedId), anyInt(), anyDouble());
    }
}
