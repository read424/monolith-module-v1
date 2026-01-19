package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import com.walrex.module_revision_tela.application.ports.output.DisableRollsPort;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.UnliftedRollProjection;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.DisableRollsQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisableRollsPersistenceAdapter implements DisableRollsPort {

    private final DisableRollsQueryRepository disableRollsQueryRepository;

    @Override
    public Flux<UnliftedRollProjection> getUnliftedRolls(Integer idPeriodo) {
        log.debug("[Adapter] getUnliftedRolls - idPeriodo: {}", idPeriodo);
        return disableRollsQueryRepository.findUnliftedRollsByPeriodo(idPeriodo)
            .doOnSubscribe(s -> log.debug("[Adapter] getUnliftedRolls - suscripción iniciada"))
            .doOnComplete(() -> log.debug("[Adapter] getUnliftedRolls - consulta completada"))
            .doOnError(error -> log.error("[Adapter] getUnliftedRolls - ERROR para periodo {}: {} - Clase: {}",
                idPeriodo, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    @Override
    public Mono<Integer> disableRollStatus(Integer idDetOrdenIngresoPeso) {
        log.debug("[Adapter] disableRollStatus - idDetOrdenIngresoPeso: {}", idDetOrdenIngresoPeso);
        return disableRollsQueryRepository.updateRollStatusToDisabled(idDetOrdenIngresoPeso)
            .doOnSubscribe(s -> log.debug("[Adapter] disableRollStatus - suscripción iniciada"))
            .doOnSuccess(count -> log.debug("[Adapter] disableRollStatus - actualización completada: {} filas", count))
            .doOnError(error -> log.error("[Adapter] disableRollStatus - ERROR para id {}: {} - Clase: {}",
                idDetOrdenIngresoPeso, error.getMessage(), error.getClass().getSimpleName(), error));
    }
}
