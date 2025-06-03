package com.walrex.role.module_role.domain.service;

import com.walrex.role.module_role.application.ports.input.RolDetailsUseCase;
import com.walrex.role.module_role.application.ports.output.RolDetailsOutputPort;
import com.walrex.role.module_role.application.ports.output.RolOutputPort;
import com.walrex.role.module_role.domain.model.RolDetailDTO;
import com.walrex.role.module_role.domain.model.RolDetailItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RolService implements RolDetailsUseCase {
    private final RolOutputPort rolOutputPort;
    private final RolDetailsOutputPort rolDetailsOutputPort;

    @Override
    public Mono<RolDetailDTO> getDetailsRolById(Long id_rol) {
        return rolOutputPort.getInfoRol(id_rol)
                .flatMap( rol -> rolDetailsOutputPort.getDetailsRoles(id_rol)
                        .map( detail -> RolDetailItemDTO.builder()
                                        .idwin_state(detail.getIdwin_state())
                                        .name_state(detail.getNo_state())
                                        .type_state(detail.getType_state())
                                        .id_parent_win(detail.getId_parent_win())
                                        .build()
                        )
                        .collectList()
                        .map(details -> RolDetailDTO.builder()
                                .id_rol(rol.getIdRol())
                                .name_rol(rol.getName_rol())
                                .details(details)
                                .build()
                        ))
                .switchIfEmpty(Mono.error(new RuntimeException("Rol no encontrado con ID: "+id_rol)));
    }
}
