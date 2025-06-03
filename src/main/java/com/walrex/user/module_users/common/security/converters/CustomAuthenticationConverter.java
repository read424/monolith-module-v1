package com.walrex.user.module_users.common.security.converters;

import com.walrex.user.module_users.common.security.authentications.CustomAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import java.util.Optional;

@Component
public class CustomAuthenticationConverter {

    public Mono<Authentication> convert(ServerWebExchange exchange){
        return Mono.just(exchange.getRequest().getHeaders())
                .map(headers -> CustomAuthentication.builder()
                    .authenticated(false)
                    .credentials(getHeaderSingleValue(headers, "password"))
                    .name(getHeaderSingleValue(headers, "username"))
                    .build()
                );
    }

    private String getHeaderSingleValue(HttpHeaders headers, String key){
        return Optional.ofNullable(headers.get(key))
            .map(l -> l.size() == 1 ? l.get(0): null )
            .orElse(null);
    }
}
