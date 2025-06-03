package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class WindowSystemDTO {
    private Long id_rol;
    private Long id_window;
    private String name_win;
    private int type_state;
    private int id_parent_win;
}
