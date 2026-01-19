package com.walrex.module_revision_tela.domain.service;

import com.walrex.module_revision_tela.application.ports.input.DisableUnliftedRollsUseCase;
import com.walrex.module_revision_tela.application.ports.output.DisableRollsPort;
import com.walrex.module_revision_tela.domain.model.dto.DisableRollsResponse;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.UnliftedRollProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisableUnliftedRollsService implements DisableUnliftedRollsUseCase {

    private final DisableRollsPort disableRollsPort;

    @Override
    @Transactional
    public Mono<DisableRollsResponse> disableUnliftedRolls(Integer idPeriodo) {
        log.info("[Service] Iniciando deshabilitación de rollos sin levantamiento para periodo: {}", idPeriodo);

        // Contadores atómicos para estadísticas
        AtomicInteger totalProcesados = new AtomicInteger(0);
        AtomicInteger rollosAlmacenDeshabilitados = new AtomicInteger(0);
        AtomicInteger rollosGuiaDeshabilitados = new AtomicInteger(0);

        return disableRollsPort.getUnliftedRolls(idPeriodo)
            .doOnNext(roll -> log.debug("[Service] Procesando rollo: idPeso={}, idAlm={}, statusAlmacen={}",
                roll.getIdDetOrdenIngresoPeso(), roll.getIdDetOrdenIngresoPesoAlm(), roll.getStatusAlmacen()))
            // Usar concatMap para procesar secuencialmente y evitar saturar conexiones R2DBC
            .concatMap(roll -> processRoll(roll, rollosAlmacenDeshabilitados, rollosGuiaDeshabilitados)
                .doOnSuccess(v -> totalProcesados.incrementAndGet())
            )
            .then(Mono.fromCallable(() -> {
                log.info("[Service] Deshabilitación completada para periodo: {} - Total: {}, Almacén: {}, Guía: {}",
                    idPeriodo, totalProcesados.get(), rollosAlmacenDeshabilitados.get(), rollosGuiaDeshabilitados.get());

                return DisableRollsResponse.builder()
                    .idPeriodo(idPeriodo)
                    .totalRollosProcesados(totalProcesados.get())
                    .rollosAlmacenDeshabilitados(rollosAlmacenDeshabilitados.get())
                    .rollosGuiaDeshabilitados(rollosGuiaDeshabilitados.get())
                    .mensaje(String.format("Proceso completado: %d rollos procesados, %d deshabilitados en almacén, %d deshabilitados en guía",
                        totalProcesados.get(), rollosAlmacenDeshabilitados.get(), rollosGuiaDeshabilitados.get()))
                    .build();
            }))
            .onErrorResume(error -> {
                log.error("[Service] Error durante deshabilitación para periodo {}: {} - Clase: {}",
                    idPeriodo, error.getMessage(), error.getClass().getSimpleName(), error);
                return Mono.error(new RuntimeException(
                    String.format("Error deshabilitando rollos para periodo %d: %s", idPeriodo, error.getMessage()), error));
            });
    }

    /**
     * Procesa un rollo individual:
     * 1. SIEMPRE deshabilita el rollo en guía de ingreso (id_detordeningresopeso_alm)
     * 2. Si status_almacen != null AND != 10, también deshabilita el rollo en almacén
     */
    private Mono<Void> processRoll(UnliftedRollProjection roll,
                                   AtomicInteger rollosAlmacenDeshabilitados,
                                   AtomicInteger rollosGuiaDeshabilitados) {

        // 1. SIEMPRE: Deshabilitar rollo en guía de ingreso (id_detordeningresopeso_alm)
        Mono<Void> disableGuia = Mono.empty();
        if (roll.getIdDetOrdenIngresoPesoAlm() != null) {
            disableGuia = disableRollsPort.disableRollStatus(roll.getIdDetOrdenIngresoPesoAlm())
                .doOnSuccess(count -> {
                    if (count > 0) {
                        rollosGuiaDeshabilitados.incrementAndGet();
                        log.debug("[Service] Deshabilitado rollo en guía: id={}", roll.getIdDetOrdenIngresoPesoAlm());
                    }
                })
                .then();
        }

        // 2. CONDICIONAL: Si status_almacen != null AND != 10, deshabilitar rollo en almacén
        Mono<Void> disableAlmacen = Mono.empty();
        if (roll.shouldDisableAlmacenRoll() && roll.getIdDetOrdenIngresoPesoAlmacen() != null) {
            disableAlmacen = disableRollsPort.disableRollStatus(roll.getIdDetOrdenIngresoPesoAlmacen())
                .doOnSuccess(count -> {
                    if (count > 0) {
                        rollosAlmacenDeshabilitados.incrementAndGet();
                        log.debug("[Service] Deshabilitado rollo en almacén: id={}", roll.getIdDetOrdenIngresoPesoAlmacen());
                    }
                })
                .then();
        }

        // Ejecutar ambas operaciones secuencialmente
        return disableGuia.then(disableAlmacen);
    }
}
