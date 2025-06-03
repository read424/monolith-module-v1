package com.walrex.user.module_users.common.security.managers;

import com.walrex.user.module_users.common.security.providers.ReverseAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@Component
@RequiredArgsConstructor
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {
    private final ReverseAuthenticationProvider reverseAuthenticationProvider;

    public Mono<Authentication> authenticate(Authentication authentication){
        return Flux.just(reverseAuthenticationProvider)
                .filter(provider -> provider.supports(authentication))
                .next()
                .flatMap(provider -> provider.authenticate(authentication));
    }
}
