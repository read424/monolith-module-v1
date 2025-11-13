package com.walrex.module_security_commons.infrastructure.adapters;

import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JwtUserContextService implements UserContextProvider {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLE_ID = "X-User-Role-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_EMPLOYEE_NAME = "X-Employee-Name";
    private static final String HEADER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_AUDIENCE = "X-Token-Audience";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Override
    public JwtUserInfo getCurrentUser(ServerRequest request) {
        log.debug("Extrayendo información del usuario desde headers");
        JwtUserInfo userInfo = JwtUserInfo.builder()
            .userId(request.headers().firstHeader(HEADER_USER_ID))
            .username(request.headers().firstHeader(HEADER_USERNAME))
            .userRoleId(request.headers().firstHeader(HEADER_USER_ROLE_ID))
            .userRole(request.headers().firstHeader(HEADER_USER_ROLE))
            .employeeName(request.headers().firstHeader(HEADER_EMPLOYEE_NAME))
            .permissions(parsePermissions(request.headers().firstHeader(HEADER_PERMISSIONS)))
            .audience(request.headers().firstHeader(HEADER_AUDIENCE))
            .hasAuthHeader(request.headers().firstHeader(HEADER_AUTHORIZATION)!=null)
            .build();

        if(log.isTraceEnabled()){
            log.trace("Usuario extraido: userId={}, username={}, permissions={}",
                    userInfo.getUserId(), userInfo.getUsername(), userInfo.getPermissions()
                );
        }
        return userInfo;
    }

    @Override
    public boolean hasPermission(ServerRequest request, String permission) {
        JwtUserInfo user = getCurrentUser(request);
        boolean hasPermission = user.hasPermission(permission);

        log.debug("Verificando permiso '{}' para usuario '{}': {}", permission, user.getUsername(), hasPermission);

        return hasPermission;
    }

    @Override
    public boolean hasAnyPermission(ServerRequest request, String... permissions) {
        JwtUserInfo user = getCurrentUser(request);
        boolean hasAny = user.hasAnyPermission(permissions);

        log.debug("Verificando permiso {} para usuario '{}': {}", Arrays.toString(permissions), user.getUsername(), hasAny);

        return hasAny;
    }

    /**
     * Parsea los permisos desde el header (separados por coma)
     *
     * @param permissionsHeader Header con permisos separados por coma
     * @return Lista de permisos
     */
    private List<String> parsePermissions(String permissionsHeader) {
        if (permissionsHeader == null || permissionsHeader.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(permissionsHeader.split(","))
            .map(String::trim)
            .filter(p -> !p.isEmpty())
            .toList();
    }
}
