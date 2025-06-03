package com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto;

import lombok.Data;

@Data
public class RecoveryPasswordRequestDTO {
    private String email;
}
