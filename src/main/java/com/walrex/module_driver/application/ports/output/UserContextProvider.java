package com.walrex.module_driver.application.ports.output;

import com.walrex.module_driver.domain.model.JwtUserInfo;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface UserContextProvider {
    JwtUserInfo getCurrentUser(ServerRequest request);
    boolean hasPermission(ServerRequest request, String permission);
    boolean hasAnyPermission(ServerRequest request, String... permissions);
}
