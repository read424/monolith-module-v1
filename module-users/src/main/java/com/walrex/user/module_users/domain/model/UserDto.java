package com.walrex.user.module_users.domain.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long idUsuario;
    private int idEmployee;
    private String username;
    private String passwordUser;
    private Boolean status;
    private int idRol;
    private LocalDate dateRegistry;
    private LocalDate dateDisabled;
    private Integer stateDefault;
    private int isAppEnabled;
    private String numberImeiPhone;
    private String tokenFcm;
    private String email;
}
