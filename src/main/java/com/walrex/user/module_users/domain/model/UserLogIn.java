package com.walrex.user.module_users.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogIn {
    private String username;
    private String password;
}
