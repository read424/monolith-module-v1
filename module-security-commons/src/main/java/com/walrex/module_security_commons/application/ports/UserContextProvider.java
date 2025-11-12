package com.walrex.module_security_commons.application.ports;

import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface UserContextProvider {
    /**
     * Obtiene la información del usuario actual desde la request
     *
     * @param request ServerRequest reactiva con headers JWT
     * @return JwtUserInfo con los datos del usuario
     */
    JwtUserInfo getCurrentUser(ServerRequest request);

    /**
     * Verifica si el usuario tiene un permiso específico
     *
     * @param request ServerRequest reactiva
     * @param permission Permiso a verificar
     * @return true si el usuario tiene el permiso
     */
    boolean hasPermission(ServerRequest request, String permission);

    /**
     * Verifica si el usuario tiene al menos uno de los permisos
     *
     * @param request ServerRequest reactiva
     * @param permissions Permisos a verificar
     * @return true si el usuario tiene al menos uno
     */
    boolean hasAnyPermission(ServerRequest request, String... permissions);

    /**
     * Verifica si el usuario tiene todos los permisos
     *
     * @param request ServerRequest reactiva
     * @param permissions Permisos a verificar
     * @return true si el usuario tiene todos
     */
    default boolean hasAllPermissions(ServerRequest request, String... permissions) {
        JwtUserInfo user = getCurrentUser(request);
        return user.hasAllPermissions(permissions);
    }
}
