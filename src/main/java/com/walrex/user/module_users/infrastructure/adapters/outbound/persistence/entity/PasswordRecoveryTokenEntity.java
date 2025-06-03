package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("seguridad.tb_password_recovery_tokens")
public class PasswordRecoveryTokenEntity {
    @Id
    private String id;

    @Column("id_usuario")
    private Long userId;

    @Column("email")
    private String email;

    @Column("codigo")
    private String code;

    @Column("fecha_creacion")
    private LocalDateTime createdAt;

    @Column("fecha_expiracion")
    private LocalDateTime expiresAt;

    @Column("intentos")
    private Integer attempts;

    @Column("estado") // 0=PENDING, 1=USED, 2=EXPIRED
    private Integer status;
}