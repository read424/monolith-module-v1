package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence;

import com.walrex.role.module_role.application.ports.output.RolOutputPort;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolEntity;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RolAdapter implements RolOutputPort {
    private final RolRepository rolRepository;

    @Override
    public Mono<RolEntity> getInfoRol(Long id_rol){
        return rolRepository.findByIdRol(id_rol);
    }
}
