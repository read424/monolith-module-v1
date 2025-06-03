package com.walrex.user.module_users.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

//@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager  implements ReactiveAuthenticationManager {
    private final JwtService jwtService;

    public Mono<Authentication> authenticate(Authentication authentication){
        String token = authentication.getCredentials().toString();
        try{
            String username = jwtService.extractUsername(token);
            if(username!=null && !jwtService.isTokenExpired(token)){
                User userDetails = new User(username, "", Collections.singletonList(new SimpleGrantedAuthority("USER")));
                return Mono.just(new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities()));
            }
        }catch(Exception e){
            return Mono.empty();
        }
        return Mono.empty();
    }
}
