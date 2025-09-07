package com.walrex.module_partidas.infrastructure.adapters.inbound.rest;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.walrex.module_partidas.application.ports.output.UserContextProvider;
import com.walrex.module_partidas.domain.model.JwtUserInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUserPartidaContextService implements UserContextProvider {
    @Override
    public JwtUserInfo getCurrentUser(ServerRequest request) {
        return JwtUserInfo.builder()
                .userId(request.headers().firstHeader("X-User-Id"))
                .username(request.headers().firstHeader("X-Username"))
                .userRoleId(request.headers().firstHeader("X-User-Role-Id"))
                .userRole(request.headers().firstHeader("X-User-Role"))
                .employeeName(request.headers().firstHeader("X-Employee-Name"))
                .permissions(parsePermissions(request.headers().firstHeader("X-User-Permissions")))
                .audience(request.headers().firstHeader("X-Token-Audience"))
                .hasAuthHeader(request.headers().firstHeader("Authorization") != null)
                .build();
    }

    @Override
    public boolean hasPermission(ServerRequest request, String permission) {
        JwtUserInfo user = getCurrentUser(request);
        return user.hasPermission(permission);
    }

    @Override
    public boolean hasAnyPermission(ServerRequest request, String... permissions) {
        JwtUserInfo user = getCurrentUser(request);
        return user.hasAnyPermission(permissions);
    }

    private List<String> parsePermissions(String permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(permissions.split(","));
    }
}
