package com.walrex.user.module_users.infrastructure.adapters.security.jwt.manager;

import com.walrex.user.module_users.infrastructure.adapters.security.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@Primary
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
            .map(auth -> jwtProvider.getClaims(auth.getCredentials().toString()))
            .log()
            .onErrorResume(e -> Mono.error(new Throwable("bad token")))
            .map( claims -> new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                Stream.of(claims.get("roles"))
                    .map(role -> (List<Map<String, String>>) role)
                    .flatMap(role -> role.stream()
                        .map( r -> r.get("authority"))
                        .map(SimpleGrantedAuthority::new)
                    ).toList()
            ));
    }
}
