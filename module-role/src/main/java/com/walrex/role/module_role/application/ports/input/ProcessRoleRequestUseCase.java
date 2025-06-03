package com.walrex.role.module_role.application.ports.input;

import com.walrex.role.module_role.domain.model.RolDetailDTO;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para procesar solicitudes de roles
 */
public interface ProcessRoleRequestUseCase {
    /**
     * Procesa una solicitud de rol
     * @param request Solicitud de rol
     * @param correlationId ID de correlación para seguimiento
     * @return Mono que completa cuando se ha procesado la solicitud
     */
    Mono<Void> process(RolDetailDTO request, String correlationId);
}
