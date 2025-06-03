package com.walrex.module_core.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class SecurityContextHolder {
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    public Mono<SecurityContext> getSecurityContext(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            String userId = getHeader(request, USER_ID_HEADER);
            String email = getHeader(request, USER_EMAIL_HEADER);
            String roles = getHeader(request, USER_ROLES_HEADER);

            return SecurityContext.builder()
                    .userId(userId)
                    .email(email)
                    .roles(parseRoles(roles))
                    .build();
        });
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    private List<String> parseRoles(String roles) {
        return roles != null ? Arrays.asList(roles.split(",")) : List.of();
    }
}
