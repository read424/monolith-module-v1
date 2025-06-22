package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence;

import com.walrex.role.module_role.application.ports.output.RolDetailsOutputPort;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.projection.RolDetails;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RolDetailsAdapter implements RolDetailsOutputPort {
    private final RolRepository rolDetailsRepository;

    @Override
    public Flux<RolDetails> getDetailsRoles(Long idrol){
        return rolDetailsRepository.findDetailsByIdRol(idrol);
    }
}
