package com.walrex.gateway.gateway.config;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DynamicModuleRouteFilterTest {
    private ModulesUrlRepository modulesUrlRepository;
    private DynamicModuleRouteFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp(){
        modulesUrlRepository = mock(ModulesUrlRepository.class);
        filter = new DynamicModuleRouteFilter(modulesUrlRepository);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void shouldRedirectRequestWhenModuleFound(){
        //Arrange
        String originalPath= "/api/v2/auth/signin";
        String expectedNewPath = "/auth/signin";

        ModulesUrl module = ModulesUrl.builder()
                .id(1L)
                .moduleName("auth")
                .path(originalPath)
                .stripPrefixCount(2)
                .uri("")
                .build();

        when(modulesUrlRepository.findByPath(originalPath))
                .thenReturn(Mono.just(module));

        MockServerHttpRequest request = MockServerHttpRequest.post(originalPath).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Capturar el exchange modificado que se pasa al siguiente filtro
        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(exchangeCaptor.capture())).thenReturn(Mono.empty());

        //Act
        GatewayFilter gatewayFilter = filter.apply(new DynamicModuleRouteFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        //Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(modulesUrlRepository).findByPath(originalPath);
        verify(chain).filter(any(ServerWebExchange.class));

        // Verificar el path en el exchange capturado
        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        assertThat(capturedExchange.getRequest().getPath().value()).isEqualTo(expectedNewPath);

        // Verificar que el atributo GATEWAY_REQUEST_URL_ATTR se configuró correctamente
        URI requestUrl = capturedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        assertThat(requestUrl).isNotNull();
        assertThat(requestUrl.getPath()).isEqualTo(expectedNewPath);
    }

    @Test
    void shouldReturnNotFoundWhenModuleNotFound() {
        // Arrange
        String originalPath = "/api/v2/unknown/path";

        when(modulesUrlRepository.findByPath(originalPath))
                .thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.post(originalPath).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        GatewayFilter gatewayFilter = filter.apply(new DynamicModuleRouteFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(modulesUrlRepository).findByPath(originalPath);
        verify(chain, never()).filter(any()); // El chain nunca debería ser llamado

        // Verificar que la respuesta tenga estado 404
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
