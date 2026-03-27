package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.SessionPesajeActivaRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.SessionPesajeCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PesajePersistenceAdapter implements PesajeOutputPort {

    private final SessionPesajeActivaRepository sessionRepository;
    private final DetalleRolloRepository rolloRepository;
    private final DetailsIngresoRepository detailsIngresoRepository;
    private final SessionPesajeCustomRepository customRepository;

    @Override
    public Mono<PesajeDetalle> findActiveSessionWithDetail() {
        return customRepository.findActiveSessionWithDetail();
    }

    @Override
    public Mono<PesajeDetalle> saveWeight(PesajeDetalle pesaje, Integer idDetOrdenIngreso) {
        DetalleRolloEntity entity = DetalleRolloEntity.builder()
                .ordenIngreso(pesaje.getIdOrdenIngreso())
                .codRollo(pesaje.getCod_rollo())
                .pesoRollo(BigDecimal.valueOf(pesaje.getPeso_rollo()))
                .idDetOrdenIngreso(idDetOrdenIngreso)
                .status(1)
                .create_at(OffsetDateTime.now())
                .update_at(OffsetDateTime.now())
                .build();

        log.info("Persistiendo rollo -> cod_rollo: {}, peso: {} kg, id_detordeningreso: {}, id_ordeningreso: {}",
                entity.getCodRollo(), entity.getPesoRollo(), entity.getIdDetOrdenIngreso(), entity.getOrdenIngreso());

        return rolloRepository.save(entity)
                .map(saved -> {
                    pesaje.setId_detordeningresopeso(saved.getId());
                    return pesaje;
                });
    }

    @Override
    public Mono<Void> incrementPesoAlmacen(Integer idDetOrdenIngreso, Double peso) {
        return detailsIngresoRepository.incrementPesoAlmacen(idDetOrdenIngreso, peso)
                .doOnNext(updatedRows -> log.info(
                        "Actualizando detordeningreso.peso_alm id_detordeningreso={}, peso_incremento={}, filas_afectadas={}"
                        , idDetOrdenIngreso,
                        peso,
                        updatedRows))
                .then();
    }

    @Override
    public Mono<String> updateSessionState(Integer sessionId) {
        return sessionRepository.updateActiveSessionAndReturnStatus();
    }

    @Override
    public Mono<Boolean> existsRolloById(Integer idDetOrdenIngresoPeso) {
        return rolloRepository.existsById(idDetOrdenIngresoPeso);
    }

    @Override
    public Mono<String> findAssignedPartidaCode(Integer idDetOrdenIngresoPeso) {
        return rolloRepository.findAssignedPartidaCode(idDetOrdenIngresoPeso);
    }

    @Override
    public Mono<Void> deleteRolloById(Integer idDetOrdenIngresoPeso) {
        return rolloRepository.deleteByIdDetOrdenIngresoPeso(idDetOrdenIngresoPeso)
                .doOnNext(deletedRows -> log.info(
                        "Eliminando rollo id_detordeningresopeso={}, filas_afectadas={}"
                        , idDetOrdenIngresoPeso,
                        deletedRows))
                .then();
    }
}
