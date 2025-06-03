package com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Table("seguridad.tbroles")
public class RolEntity {
    @Id
    @Column("id_rol")
    private Long idRol;

    @Column("no_rol")
    private String name_rol;

    @Column("no_descrip")
    private String detail_rol;

    @Column("il_estado")
    private Boolean status;
}
