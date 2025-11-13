package com.walrex.module_security_commons.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtUserInfo {
    private String userId;
    private String username;
    private String userRoleId;
    private String userRole;
    private String employeeName;

    @Builder.Default
    private List<String> permissions = Collections.emptyList();

    private String audience;
    private boolean hasAuthHeader;

    /**
    * Verifica si el usuario tiene un permiso específico
    *
    * @param permission Permiso a verificar (ej: "almacen:kardex:read")
    * @return true si el usuario tiene el permiso
    */
    public boolean hasPermission(String permission){
        return permissions != null && permissions.contains(permission);
    }

    /**
    * Verifica si el usuario tiene al menos uno de los permisos especificados
    *
    * @param permissions Permisos a verificar
    * @return true si el usuario tiene al menos uno
    */
    public boolean hasAnyPermission(String... permissions){
        if(this.permissions == null || this.permissions.isEmpty()){
            return false;
        }
        return Arrays.stream(permissions)
                .anyMatch(this.permissions::contains);
    }

    /**
    * Verifica si el usuario tiene todos los permisos especificados
    *
    * @param permissions Permisos a verificar
    * @return true si el usuario tiene todos
    */
    public boolean hasAllPermissions(String... permissions){
        if(this.permissions == null || this.permissions.isEmpty()){
            return false;
        }
        return Arrays.stream(permissions)
                .allMatch(this.permissions::contains);
    }

    /**
    * Verifica si la información del usuario es válida
    *
    * @return true si tiene userId y username
    */
    public boolean isValid() {
        return userId != null && !userId.isBlank() && username != null && !username.isBlank();
    }

    /**
    * Verifica si el usuario está autenticado
    *
    * @return true si tiene header de autorización y datos válidos
    */
    public boolean isAuthenticated(){
        return hasAuthHeader && isValid();
    }
}
