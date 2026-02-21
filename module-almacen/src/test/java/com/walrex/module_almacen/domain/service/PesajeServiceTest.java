package com.walrex.module_almacen.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PesajeServiceTest {

    @Mock
    private PesajeOutputPort pesajeRepository;

    @Mock
    private PesajeNotificationPort notificationPort;

    private PesajeService pesajeService;

    @BeforeEach
    void setUp() {
        pesajeService = new PesajeService(pesajeRepository, notificationPort);
    }

    @Test
    void registrarPesaje_Success() {
        // Given
        PesajeRequest request = new PesajeRequest(10.5);
        PesajeDetalle initialDetalle = PesajeDetalle.builder()
                .id_ordeningreso(1)
                .cod_rollo("LOTE-1")
                .cnt_registrados(0)
                .id_detordeningreso_hidden(100)
                .id_session_hidden(50)
                .build();

        PesajeDetalle savedDetalle = PesajeDetalle.builder()
                .id_detordeningresopeso(1)
                .id_ordeningreso(1)
                .peso_rollo(10.5)
                .cod_rollo("LOTE-1")
                .cnt_registrados(0)
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
                .build();

        PesajeDetalle savedDetalle = PesajeDetalle.builder()
                .id_session_hidden(50)
                .cnt_registrados(9)
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
}
