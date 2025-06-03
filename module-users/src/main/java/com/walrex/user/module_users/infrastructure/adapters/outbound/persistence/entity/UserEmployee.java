package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class UserEmployee {
    private Long id;
    private Long id_usuario;
    private String no_usuario;
    private Long id_empleado;
    private Boolean il_estado;
    private Long idrol_sistema;
    private Long state_default;
    private Long id_tipoper;
    private String no_apepat;
    private String no_nombres;
    private Long id_det_personal;
    private Long id_area;
    private String no_area;
    private Long status;
    private Long id_status;
}
