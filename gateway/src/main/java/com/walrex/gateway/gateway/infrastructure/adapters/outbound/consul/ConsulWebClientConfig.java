package com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul;

import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ConsulWebClientConfig {

    @Bean
    WebClient consulWebClient(
        WebClient.Builder builder,
        @Value("${consul.base-url}") String baseUrl,
        @Value("${consul.token}") String token
    ) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeaders(h -> {
                h.set("X-Consul-Token", token);
                h.set(HttpHeaders.ACCEPT, "application/json");
            })
            .build();
    }
}
