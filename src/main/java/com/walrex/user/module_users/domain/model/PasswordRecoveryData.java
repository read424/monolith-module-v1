package com.walrex.user.module_users.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PasswordRecoveryData {
    // Propiedades planas en lugar de objeto anidado
    private final Long userId;
    private final String username;
    private final String email;
    private final String recoveryCode;
}
