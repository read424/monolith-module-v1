package com.walrex.role.module_role.application.ports.output;

import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolEntity;
import reactor.core.publisher.Mono;

public interface RolOutputPort {
    Mono<RolEntity> getInfoRol(Long id_rol);
}
