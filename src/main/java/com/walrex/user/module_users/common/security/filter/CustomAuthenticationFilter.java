package com.walrex.user.module_users.common.security.filter;

import com.walrex.user.module_users.common.security.converters.CustomAuthenticationConverter;
import com.walrex.user.module_users.common.security.managers.CustomAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomAuthenticationFilter implements WebFilter {
    private final CustomAuthenticationConverter customAuthenticationConverter;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final ServerWebExchangeMatcher requestMatcher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        return requestMatcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .flatMap(matchResult -> customAuthenticationConverter.convert(exchange))
                .flatMap(customAuthenticationManager::authenticate)
                .flatMap(authentication -> onAuthenticationSuccess(authentication, new WebFilterExchange(exchange, chain)))
                .then();
    }

    private Mono<Void> onAuthenticationSuccess(Authentication authentication, WebFilterExchange exchange){
        ServerWebExchange serverWebExchange = exchange.getExchange();
        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return exchange.getChain().filter(serverWebExchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }
}
