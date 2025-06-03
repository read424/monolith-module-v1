package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Table("seguridad.tbusuarios")
public class UserEntity {
    @Id
    private Long id_usuario;

    @Column("id_empleado")
    private int id_employee;

    @Column("no_usuario")
    private String username;

    @Column("no_passwd")
    private String password_user;

    @Column("il_estado")
    private Boolean status;

    @Column("idrol_sistema")
    private int id_rol;

    @Column("fec_ingreso")
    private LocalDate date_registry;

    @Column("fec_inactivo")
    private LocalDate date_disabled;

    private Integer state_default;

    @Column("use_app")
    private int is_app_enabled;

    @Column("imei_phone")
    private String number_imei_phone;

    private String token_fcm;

    private String email;
}
