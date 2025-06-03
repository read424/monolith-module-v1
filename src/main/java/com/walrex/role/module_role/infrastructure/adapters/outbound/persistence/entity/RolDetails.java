package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class RolDetails {
    private Long id_rol;
    private Long idwin_state;
    private String no_state;
    private Integer type_state;
    private Integer id_parent_win;
}
