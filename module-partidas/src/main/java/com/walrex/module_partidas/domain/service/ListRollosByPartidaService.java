package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.ListRollosByPartidaUseCase;
import com.walrex.module_partidas.application.ports.output.ListRollosByPartidaPort;
import com.walrex.module_partidas.domain.model.RolloPartida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ListRollosByPartidaService implements ListRollosByPartidaUseCase {

    private final ListRollosByPartidaPort listRollosByPartidaPort;

    @Override
    public Flux<RolloPartida> listRollos(Integer idPartida) {
        return listRollosByPartidaPort.listRollos(idPartida);
    }
}
