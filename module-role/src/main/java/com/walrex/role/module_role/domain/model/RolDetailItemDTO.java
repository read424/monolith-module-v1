package com.walrex.role.module_role.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDetailItemDTO {
    private Long idwin_state;
    private String name_state;
    private Integer id_parent_win;
    private Integer type_state;
}
