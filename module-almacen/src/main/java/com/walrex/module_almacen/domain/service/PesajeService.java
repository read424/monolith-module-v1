package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PesajeService implements PesajeUseCase {

    private final PesajeOutputPort pesajeRepository;
    private final PesajeNotificationPort notificationPort;

    @Override
    @Transactional
    public Mono<PesajeDetalle> registrarPesaje(PesajeRequest request) {
        log.info("Iniciando registro de pesaje: {} kg", request.getPeso());
        
        return pesajeRepository.findActiveSessionWithDetail()
                .flatMap(detalle -> {
                    detalle.setPeso_rollo(request.getPeso());
                    return pesajeRepository.saveWeight(detalle, detalle.getId_detordeningreso())
                            .flatMap(savedDetalle -> 
                                pesajeRepository.updateSessionState(savedDetalle.getId_session_hidden())
                                    .map(newStatus -> {
                                        savedDetalle.setCompletado("0".equals(newStatus));
                                        savedDetalle.setCnt_registrados(savedDetalle.getCnt_registrados() + 1);
                                        return savedDetalle;
                                    })
                            );
                })
                .flatMap(savedDetalle -> notificationPort.notifyWeightRegistered(savedDetalle)
                        .thenReturn(savedDetalle))
                .doOnSuccess(detalle -> log.info("Pesaje registrado y notificado: {}", detalle.getCod_rollo()))
                .doOnError(error -> log.error("Error al registrar pesaje: {}", error.getMessage()));
    }
}
