package com.walrex.gateway.gateway.config;

import com.walrex.gateway.gateway.application.ports.output.ServiceRegistryPort;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.repository.ModulesUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DynamicModuleRouteFilterTest {

    @Mock
    private ModulesUrlRepository modulesUrlRepository;

    @Mock
    private ServiceRegistryPort serviceRegistryPort;

    private RouteResolver routeResolver;
    private PathTransformer pathTransformer;
    private ProxyRequestBuilder proxyRequestBuilder;
    private DynamicModuleRouteFilter filter;

    @BeforeEach
    void setUp() {
        routeResolver = new RouteResolver(modulesUrlRepository);
        pathTransformer = new PathTransformer();
        proxyRequestBuilder = new ProxyRequestBuilder(pathTransformer);
        filter = new DynamicModuleRouteFilter(routeResolver, pathTransformer, proxyRequestBuilder, serviceRegistryPort);
    }

    // ── PathTransformer tests ─────────────────────────────────────────────────

    @Test
    void testStripPrefix_Strip2Segments() {
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(2);

        String result = pathTransformer.stripPrefix("/api/v2/almacen/ingreso-logistica", module);

        assertEquals("/almacen/ingreso-logistica", result);
    }

    @Test
    void testStripPrefix_NoStrip() {
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(null);

        String result = pathTransformer.stripPrefix("/api/v2/almacen/ingreso-logistica", module);

        assertEquals("/api/v2/almacen/ingreso-logistica", result);
    }

    @Test
    void testStripPrefix_StripZero() {
        ModulesUrl module = new ModulesUrl();
        module.setStripPrefixCount(0);

        String result = pathTransformer.stripPrefix("/api/v2/almacen/ingreso-logistica", module);

        assertEquals("/api/v2/almacen/ingreso-logistica", result);
    }

    // ── RouteResolver tests ───────────────────────────────────────────────────

    @Test
    void testFindModuleByPattern_Found() {
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

        when(modulesUrlRepository.findAll()).thenReturn(Flux.just(module1, module2));

        StepVerifier.create(routeResolver.findModuleByPattern(requestPath))
            .expectNext(module1)
            .verifyComplete();
    }

    @Test
    void testFindModuleByPattern_NotFound() {
        String requestPath = "/api/v2/reports/daily";

        ModulesUrl module1 = ModulesUrl.builder()
            .path("/api/v2/almacen/**")
            .stripPrefixCount(2)
            .moduleName("almacen")
            .build();

        when(modulesUrlRepository.findAll()).thenReturn(Flux.just(module1));

        StepVerifier.create(routeResolver.findModuleByPattern(requestPath))
            .verifyComplete();
    }

    // ── DynamicModuleRouteFilter tests ────────────────────────────────────────

    @Test
    void testProcessRouting_Success() {
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

        StepVerifier.create(filter.processRouting(module, requestPath, request, exchange, chain))
            .verifyComplete();
    }

    @Test
    void testProcessRouting_VerifyForwardUri() {
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

        filter.processRouting(module, requestPath, request, exchange, chain);

        verify(attributes).put("GATEWAY_FORWARDED_REQUEST", true);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(exchange.getAttributes()).put(eq(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR), uriCaptor.capture());

        assertEquals("forward:/almacen/ingreso-logistica", uriCaptor.getValue().toString());
    }
}
