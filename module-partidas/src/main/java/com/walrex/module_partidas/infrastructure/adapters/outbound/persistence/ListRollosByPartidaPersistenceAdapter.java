package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ListRollosByPartidaPort;
import com.walrex.module_partidas.domain.model.RolloPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.RolloPartidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ListRollosByPartidaPersistenceAdapter implements ListRollosByPartidaPort {

    private final RolloPartidaRepository repository;

    @Override
    public Flux<RolloPartida> listRollos(Integer idPartida) {
        return repository.findRollosByPartida(idPartida);
    }
}
