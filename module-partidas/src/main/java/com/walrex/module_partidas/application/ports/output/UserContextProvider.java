package com.walrex.module_partidas.application.ports.output;

import org.springframework.web.reactive.function.server.ServerRequest;

import com.walrex.module_partidas.domain.model.JwtUserInfo;

public interface UserContextProvider {
    JwtUserInfo getCurrentUser(ServerRequest request);
    boolean hasPermission(ServerRequest request, String permission);
    boolean hasAnyPermission(ServerRequest request, String... permissions);
}
