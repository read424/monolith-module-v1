package com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ConsulWebClientConfig {

    @Bean
    WebClient consulWebClient(
        WebClient.Builder builder,
        @Value("${consul.base-url}") String baseUrl,
        @Value("${consul.token}") String token,
        @Value("${consul.connect-timeout-ms:2000}") int connectTimeoutMs,
        @Value("${consul.read-timeout-ms:3000}") int readTimeoutMs
    ) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(readTimeoutMs));

        return builder
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(baseUrl)
            .defaultHeaders(h -> {
                h.set("X-Consul-Token", token);
                h.set(HttpHeaders.ACCEPT, "application/json");
            })
            .build();
    }
}
