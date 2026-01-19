package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowInventoryLiftingRoll;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowLevantamientoProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AnalysisLiftingRevisionPort {

    Flux<RowLevantamientoProjection> getLiftingInventory(Integer idPeriodo);

    Flux<RowInventoryLiftingRoll> getRollosObservationInventory(Integer idPeriodo, Integer idDetOrdenIngreso);

    /**
     * Actualiza múltiples rollos con el id_levantamiento especificado
     * @param detailRolloRevisionIds Lista de IDs (PK de tabla detail_rollo_revision)
     * @param idLevantamiento ID del levantamiento a asignar
     * @return Mono con el número de filas actualizadas
     */
    Mono<Integer> updateRollosWithLevantamientoId(List<Integer> detailRolloRevisionIds, Integer idLevantamiento);

    /**
     * Decrementa la cantidad_disponible del levantamiento
     * Si cantidad_disponible es NULL, se inicializa con cnt_rollos antes de decrementar
     * @param idLevantamiento ID del levantamiento
     * @param cantidad Cantidad a decrementar
     * @return Mono con el número de filas actualizadas
     */
    Mono<Integer> decrementarCantidadDisponible(Integer idLevantamiento, Integer cantidad);
}
