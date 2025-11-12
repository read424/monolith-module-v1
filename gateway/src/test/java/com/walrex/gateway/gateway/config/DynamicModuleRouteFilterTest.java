package com.walrex.gateway.gateway.config;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.ConsulServiceResolver;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DynamicModuleRouteFilterTest {
    @Mock
    private ModulesUrlRepository modulesUrlRepository;

    @Mock
    private ConsulServiceResolver consulServiceResolver;

    private DynamicModuleRouteFilter filter;

    private GatewayFilterChain chain;

    @BeforeEach
    void setUp(){
        filter = new DynamicModuleRouteFilter(modulesUrlRepository, consulServiceResolver);
    }

    @Test
    void testProcessPath_Strip2Segments() {
        // Given
        String requestPath = "/api/v2/almacen/ingreso-logistica";
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(2);

        // When
        String result = filter.processPath(requestPath, module);

        // Then
        assertEquals("/almacen/ingreso-logistica", result);
    }

    @Test
    void testProcessPath_NoStrip() {
        // Given
        String requestPath = "/api/v2/almacen/ingreso-logistica";
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(null); // Sin strip

        // When
        String result = filter.processPath(requestPath, module);

        // Then
        assertEquals("/api/v2/almacen/ingreso-logistica", result);
    }

    @Test
    void testProcessPath_StripZero() {
        // Given
        String requestPath = "/api/v2/almacen/ingreso-logistica";
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(0);

        // When
        String result = filter.processPath(requestPath, module);

        // Then
        assertEquals("/api/v2/almacen/ingreso-logistica", result);
    }

    @Test
    void testProcessRouting_Success() {
        //Given
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(2);

        String requestPath = "/api/v2/almacen/ingreso-logistica";

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class);
        ServerHttpRequest modifiedRequest = mock(ServerHttpRequest.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        ServerWebExchange mutatedExchange = mock(ServerWebExchange.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        URI mockUri = URI.create("http://localhost/api/v2/almacen/ingreso-logistica");
        Map<String, Object> attributes = new HashMap<>();

        when(request.getURI()).thenReturn(mockUri);
        when(request.getBody()).thenReturn(Flux.empty());
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.path(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(modifiedRequest);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(mutatedExchange);

        when(chain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.processRouting(module, requestPath, request, exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testProcessRouting_VerifyForwardUri() {
        // Given
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(2);

        String requestPath = "/api/v2/almacen/ingreso-logistica";

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class);
        ServerHttpRequest modifiedRequest = mock(ServerHttpRequest.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        ServerWebExchange mutatedExchange = mock(ServerWebExchange.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        URI mockUri = URI.create("http://localhost/api/v2/almacen/ingreso-logistica");
        Map<String, Object> attributes = mock(Map.class);

        when(request.getURI()).thenReturn(mockUri);
        when(request.getBody()).thenReturn(Flux.empty());
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.path(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(modifiedRequest);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(mutatedExchange);

        lenient().when(chain.filter(any())).thenReturn(Mono.empty());

        // When
        filter.processRouting(module, requestPath, request, exchange, chain);

        // Then - Verificar GATEWAY_FORWARDED_REQUEST
        verify(attributes).put("GATEWAY_FORWARDED_REQUEST", true);

        // Then
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(exchange.getAttributes()).put(eq(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR), uriCaptor.capture());

        URI capturedUri = uriCaptor.getValue();
        assertEquals("forward:/almacen/ingreso-logistica", capturedUri.toString());
    }

    @Test
    void testFindModuleByPattern_Found() {
        // Given
        String requestPath = "/api/v2/almacen/ingreso-logistica";

        ModulesUrl module1 = ModulesUrl.builder()
                .path("/api/v2/almacen/**")
                .stripPrefixCount(2)
                .moduleName("almacen")
                .build();
        ModulesUrl module2 = ModulesUrl.builder()
                .path("/api/v2/user/**")
                .stripPrefixCount(2)
                .moduleName("user")
                .build();

        when(modulesUrlRepository.findAll())
                .thenReturn(Flux.just(module1, module2));

        // When
        Mono<ModulesUrl> result = filter.findModuleByPattern(requestPath);

        // Then
        StepVerifier.create(result)
                .expectNext(module1)
                .verifyComplete();
    }

    @Test
    void testFindModuleByPattern_NotFound() {
        // Given
        String requestPath = "/api/v2/reports/daily";

        ModulesUrl module1 = ModulesUrl.builder()
                .path("/api/v2/almacen/**")
                .stripPrefixCount(2)
                .moduleName("almacen")
                .build();

        when(modulesUrlRepository.findAll())
                .thenReturn(Flux.just(module1));

        // When
        Mono<ModulesUrl> result = filter.findModuleByPattern(requestPath);

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Sin elementos
    }
}
