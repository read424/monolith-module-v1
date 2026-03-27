package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.SessionArticuloPesajeOutputPort;
import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.SessionPesajeActiva;
import com.walrex.module_almacen.domain.model.dto.RolloPesadoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.SessionPesajeActivaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.SessionPesajeActivaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.SessionPesajeActivaRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.SessionPesajeCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionArticuloPesajePersistenceAdapter implements SessionArticuloPesajeOutputPort {

    private final SessionPesajeActivaRepository repository;
    private final SessionPesajeCustomRepository customRepository;
    private final SessionPesajeActivaMapper sessionPesajeActivaMapper;

    @Override
    public Mono<SessionPesajeActiva> findStatusByIdDetOrdenIngreso(Integer idDetOrdenIngreso) {
        return repository.findByIdDetOrdenIngreso(idDetOrdenIngreso)
                .map(sessionPesajeActivaMapper::toDomain);
    }

    @Override
    public Mono<ArticuloPesajeSession> getArticuloWithSessionDetail(Integer idDetOrdenIngreso) {
        return customRepository.findArticuloWithSessionDetail(idDetOrdenIngreso);
    }

    @Override
    public Mono<Void> insertSession(Integer idDetOrdenIngreso, Integer cntRollos, Double totKg) {
        SessionPesajeActivaEntity entity = SessionPesajeActivaEntity.builder()
                .idDetOrdenIngreso(idDetOrdenIngreso)
                .cntRollos(cntRollos)
                .totKg(totKg)
                .cntRegistro(0)
                .status("1")
                .build();
        return repository.save(entity).then();
    }

    @Override
    public Mono<Void> updateSessionStatusToCompleted(Integer sessionId) {
        return repository.updateStatusToCompletedById(sessionId).then();
    }

    @Override
    public Mono<Void> updateSessionStatusToActive(Integer sessionId) {
        return repository.updateStatusToActiveById(sessionId).then();
    }

    @Override
    public Flux<RolloPesadoDTO> findRollosByIdDetOrdenIngreso(Integer idDetOrdenIngreso) {
        return customRepository.findRollosByIdDetOrdenIngreso(idDetOrdenIngreso);
    }
}
