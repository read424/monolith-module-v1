package com.walrex.role.module_role.application.ports.input;

import com.walrex.role.module_role.domain.model.RolDetailDTO;
import reactor.core.publisher.Mono;

public interface RolDetailsUseCase {
    Mono<RolDetailDTO> getDetailsRolById(Long id_rol);
}
