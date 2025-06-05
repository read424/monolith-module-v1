package com.walrex.user.module_users.infrastructure.adapters.security.jwt.manager;

import com.walrex.user.module_users.infrastructure.adapters.security.jwt.provider.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
            .map(auth -> jwtProvider.getClaims(auth.getCredentials().toString()))
            .log()
            .onErrorResume(e -> Mono.error(new Throwable("bad token")))
            .map(this::createAuthenticationToken);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(Claims claims){
        String subject = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims){
        Object rolesObj = claims.get("roles");
        if(rolesObj==null){
            log.debug("No roles found in JWT claims, using empty authorities");
            return Collections.emptyList();
        }
        try{
            @SuppressWarnings("unchecked")
            List<Map<String, String>> rolesList = (List<Map<String, String>>) rolesObj;

            return rolesList.stream()
                    .filter(Objects::nonNull) // ✅ Filtrar nulls
                    .map(role -> role.get("authority"))
                    .filter(Objects::nonNull) // ✅ Filtrar authorities null
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }catch(ClassCastException e){
            log.warn("Invalid roles format in JWT claims: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
