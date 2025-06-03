package com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto;

import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsResponseDTO {
    private String id_usuario;
    private String no_usuario;
    private Long id_rol;
    private String no_rol;
    private String id_empleado;
    private String no_empleado;
    private String id_area;
    private String state_default;

    @Builder.Default
    private Map<String, List<String>> state_menu = Collections.emptyMap();

    @Builder.Default
    private List<String> permissions = Collections.emptyList();
}
