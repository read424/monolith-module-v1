package com.walrex.role.module_role.application.ports.output;

import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolDetails;
import reactor.core.publisher.Flux;

public interface RolDetailsOutputPort {
    Flux<RolDetails> getDetailsRoles(Long idrol);
}
