package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Table("seguridad.tbroles_detalles")
public class RolesDetailsEntity {

    private Long id_rol;

    @Column("idwin_state")
    private Long id_window;
}
