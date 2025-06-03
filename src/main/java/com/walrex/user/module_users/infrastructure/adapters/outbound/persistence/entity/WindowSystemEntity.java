package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity;

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
@Table("seguridad.win_sistemas")
public class WindowSystemEntity {

    @Id
    private Long idwin_state;

    @Column("no_state")
    private String name_win;

    private int type_state;

    private int id_parent_win;
}
