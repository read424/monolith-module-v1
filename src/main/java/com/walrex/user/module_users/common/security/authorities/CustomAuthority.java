package com.walrex.user.module_users.common.security.authorities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@Getter
public class CustomAuthority implements GrantedAuthority {
    private String authority;
}
