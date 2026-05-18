package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.RolloPartida;
import reactor.core.publisher.Flux;

public interface ListRollosByPartidaPort {
    Flux<RolloPartida> listRollos(Integer idPartida);
}
