package com.walrex.user.module_users.common.security.providers;

import com.walrex.user.module_users.common.security.authentications.CustomAuthentication;
import com.walrex.user.module_users.common.security.authorities.CustomAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class ReverseAuthenticationProvider implements CustomAuthenticationProvider {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication){
        return Mono.just(authentication)
                .filter(this::supports)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid Authentication")))
                .flatMap(auth -> this.reverseAuthenticate((CustomAuthentication) auth));
    }

    private Mono<CustomAuthentication> reverseAuthenticate(CustomAuthentication authentication){
        return Mono.just(authentication)
                .filter(auth -> Objects.equals(
                        auth.getName(),
                        new StringBuilder().append(auth.getCredentials()).reverse().toString())
                )
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Authentication Failed")))
                .map(auth -> CustomAuthentication.builder()
                        .authenticated(true)
                        .authorities(List.of(new CustomAuthority("ROLE_CustomRole")))
                        .principal(auth.getName())
                        .build()
                );
    }

    @Override
    public Boolean supports(Authentication authentication) {
        return authentication instanceof CustomAuthentication;
    }
}
