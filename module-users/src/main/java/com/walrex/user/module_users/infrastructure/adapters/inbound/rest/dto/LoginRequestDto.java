package com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class LoginRequestDto {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "El password es obligatorio")
    private String password;
}
