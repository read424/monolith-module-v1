package com.walrex.role.module_role.application.ports.input;

import reactor.core.publisher.Mono;

public interface RoleMessageUseCase {
    Mono<Void> processRoleMessage(Long idRol, String correlationId);
}
