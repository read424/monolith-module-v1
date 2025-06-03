package com.walrex.user.module_users.infrastructure.adapters.security.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDetailDTO implements UserDetails {
    private String id_usuario;
    private String no_usuario;
    private Long id_rol;
    private String no_rol;
    private String id_empleado;
    private String no_empleado;
    private String id_area;
    private String state_default;
    @Builder.Default
    private Map<String, List<String>> state_menu = Collections.emptyMap();
    @Builder.Default
    private List<String> permissions = Collections.emptyList();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Agregar rol principal
        if (no_rol != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + no_rol.toUpperCase()));
        }

        // Agregar permisos individuales
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return no_usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Personaliza según tu lógica de negocio
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Personaliza según tu lógica de negocio
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Personaliza según tu lógica de negocio
    }

    @Override
    public boolean isEnabled() {
        return true; // Personaliza según tu lógica de negocio
    }
}
