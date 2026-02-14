package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.PesajePersistenceMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.SessionPesajeActivaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PesajePersistenceAdapter implements PesajeOutputPort {

    private final SessionPesajeActivaRepository sessionRepository;
    private final DetalleRolloRepository rolloRepository;
    private final PesajePersistenceMapper mapper;

    @Override
    public Mono<PesajeDetalle> findActiveSessionWithDetail() {
        return sessionRepository.findActiveSessionWithDetail()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PesajeDetalle> saveWeight(PesajeDetalle pesaje, Integer idDetOrdenIngreso) {
        DetalleRolloEntity entity = DetalleRolloEntity.builder()
                .ordenIngreso(pesaje.getId_ordeningreso())
                .codRollo(pesaje.getCod_rollo())
                .pesoRollo(BigDecimal.valueOf(pesaje.getPeso_rollo()))
                .idDetOrdenIngreso(idDetOrdenIngreso)
                .status(1)
                .create_at(OffsetDateTime.now())
                .update_at(OffsetDateTime.now())
                .build();

        return rolloRepository.save(entity)
                .map(saved -> {
                    pesaje.setId_detordeningresopeso(saved.getId());
                    return pesaje;
                });
    }

    @Override
    public Mono<String> updateSessionState(Integer sessionId) {
        return sessionRepository.updateActiveSessionAndReturnStatus();
    }
}
