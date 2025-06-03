package com.walrex.user.module_users.common.security.providers;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public interface CustomAuthenticationProvider {
    Mono<Authentication> authenticate(Authentication authentication);

    Boolean supports(Authentication authentication);
}
