package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import com.walrex.module_revision_tela.application.ports.output.AnalysisLiftingRevisionPort;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowInventoryLiftingRoll;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowLevantamientoProjection;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.InventoryLiftingQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryLiftingPersistenceAdapter implements AnalysisLiftingRevisionPort {

    private final InventoryLiftingQueryRepository liftingQueryRepository;

    @Override
    public Flux<RowLevantamientoProjection> getLiftingInventory(Integer idPeriodo){
        log.debug("[Adapter] getLiftingInventory - iniciando consulta para periodo: {}", idPeriodo);
        return liftingQueryRepository.getLevantamientoByIdPeriodo(idPeriodo)
            .doOnSubscribe(s -> log.debug("[Adapter] getLiftingInventory - suscripción iniciada para periodo: {}", idPeriodo))
            .doOnNext(row -> log.trace("[Adapter] getLiftingInventory - fila obtenida: idDetOrden={}", row.getIdDetOrdenIngreso()))
            .doOnComplete(() -> log.debug("[Adapter] getLiftingInventory - consulta completada para periodo: {}", idPeriodo))
            .doOnError(error -> log.error("[Adapter] getLiftingInventory - ERROR para periodo {}: {} - Clase: {}",
                idPeriodo, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    @Override
    public Flux<RowInventoryLiftingRoll> getRollosObservationInventory(Integer idPeriodo, Integer idDetOrdenIngreso){
        log.debug("[Adapter] getRollosObservationInventory - iniciando consulta para periodo: {}, idDetOrdenIngreso: {}",
            idPeriodo, idDetOrdenIngreso);
        return liftingQueryRepository.getRollsAtInventory(idPeriodo, idDetOrdenIngreso)
            .doOnSubscribe(s -> log.debug("[Adapter] getRollosObservationInventory - suscripción iniciada"))
            .doOnNext(row -> log.trace("[Adapter] getRollosObservationInventory - rollo obtenido: id={}, partida={}",
                row.getId(), row.getId_partida()))
            .doOnComplete(() -> log.debug("[Adapter] getRollosObservationInventory - consulta completada"))
            .doOnError(error -> log.error("[Adapter] getRollosObservationInventory - ERROR para periodo={}, idDetOrdenIngreso={}: {} - Clase: {}",
                idPeriodo, idDetOrdenIngreso, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    @Override
    public Mono<Integer> updateRollosWithLevantamientoId(List<Integer> detailRolloRevisionIds, Integer idLevantamiento){
        log.debug("[Adapter] updateRollosWithLevantamientoId - iniciando actualización de {} rollos con idLevantamiento: {}",
            detailRolloRevisionIds != null ? detailRolloRevisionIds.size() : 0, idLevantamiento);
        return liftingQueryRepository.updateRollosWithLevantamiento(detailRolloRevisionIds, idLevantamiento)
            .doOnSubscribe(s -> log.debug("[Adapter] updateRollosWithLevantamientoId - suscripción iniciada"))
            .doOnSuccess(count -> log.debug("[Adapter] updateRollosWithLevantamientoId - actualización completada: {} filas",count))
            .doOnError(error -> log.error("[Adapter] updateRollosWithLevantamientoId - ERROR actualizando rollos={}, idLevantamiento={}: {} - Clase: {}",
                detailRolloRevisionIds, idLevantamiento, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    @Override
    public Mono<Integer> decrementarCantidadDisponible(Integer idLevantamiento, Integer cantidad){
        log.debug("[Adapter] decrementarCantidadDisponible - idLevantamiento: {}, cantidad: {}", idLevantamiento, cantidad);
        return liftingQueryRepository.decrementarCantidadDisponibleLevantamiento(idLevantamiento, cantidad)
            .doOnSubscribe(s -> log.debug("[Adapter] decrementarCantidadDisponible - suscripción iniciada"))
            .doOnSuccess(count -> log.debug("[Adapter] decrementarCantidadDisponible - actualización completada: {} filas", count))
            .doOnError(error -> log.error("[Adapter] decrementarCantidadDisponible - ERROR para idLevantamiento={}, cantidad={}: {} - Clase: {}",
                idLevantamiento, cantidad, error.getMessage(), error.getClass().getSimpleName(), error));
    }
}
