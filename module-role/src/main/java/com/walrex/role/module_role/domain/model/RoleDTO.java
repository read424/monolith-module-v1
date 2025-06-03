package com.walrex.role.module_role.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private Long id_rol;
    private String name_rol;
    private String det_rol;
    private Boolean status;
}
