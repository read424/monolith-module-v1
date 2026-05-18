package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.RolloPartida;
import reactor.core.publisher.Flux;

public interface ListRollosByPartidaUseCase {
    Flux<RolloPartida> listRollos(Integer idPartida);
}
