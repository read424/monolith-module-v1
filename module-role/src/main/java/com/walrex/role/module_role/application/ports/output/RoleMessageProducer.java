package com.walrex.role.module_role.application.ports.output;

import com.walrex.role.module_role.domain.model.RolDetailDTO;
import reactor.core.publisher.Mono;

public interface RoleMessageProducer {
    Mono<Void> sendMessage(RolDetailDTO message, String correlationId);
}
