package com.walrex.role.module_role.domain.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDetailDTO {
    private Long id_rol;
    private String name_rol;
    private String det_rol;
    private Boolean status;
    private List<RolDetailItemDTO> details;
}
