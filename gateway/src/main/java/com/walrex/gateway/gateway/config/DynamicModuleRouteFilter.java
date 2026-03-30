package com.walrex.gateway.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;

import com.walrex.gateway.gateway.application.ports.output.ServiceRegistryPort;
import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DynamicModuleRouteFilter extends AbstractGatewayFilterFactory<DynamicModuleRouteFilter.Config> {

    private static final int MAX_ROUTING_ATTEMPTS = 3;
    private static final int MAX_HOP_COUNT = 3;
    private static final String ROUTING_ATTEMPT_KEY = "ROUTING_ATTEMPT_COUNT";

    private final RouteResolver routeResolver;
    private final PathTransformer pathTransformer;
    private final ProxyRequestBuilder proxyRequestBuilder;
    private final ServiceRegistryPort serviceRegistryPort;

    private final Counter routingInternal;
    private final Counter routingExternal;
    private final Counter routingConsul;
    private final Timer routingTimer;

    public DynamicModuleRouteFilter(
        RouteResolver routeResolver,
        PathTransformer pathTransformer,
        ProxyRequestBuilder proxyRequestBuilder,
        ServiceRegistryPort serviceRegistryPort,
        MeterRegistry meterRegistry
    ) {
        super(Config.class);
        this.routeResolver = routeResolver;
        this.pathTransformer = pathTransformer;
        this.proxyRequestBuilder = proxyRequestBuilder;
        this.serviceRegistryPort = serviceRegistryPort;
        this.routingInternal = Counter.builder("gateway.routing").tag("type", "internal").register(meterRegistry);
        this.routingExternal = Counter.builder("gateway.routing").tag("type", "external").register(meterRegistry);
        this.routingConsul   = Counter.builder("gateway.routing").tag("type", "consul").register(meterRegistry);
        this.routingTimer    = Timer.builder("gateway.routing.duration").register(meterRegistry);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            log.debug("DynamicModuleRouteFilter - Path: '{}'", path);

            // Detectar bucle cross-process via header
            String hopHeader = exchange.getRequest().getHeaders().getFirst("X-Gateway-Hop-Count");
            if (hopHeader != null) {
                try {
                    int hops = Integer.parseInt(hopHeader);
                    if (hops >= MAX_HOP_COUNT) {
                        log.error("Demasiados saltos cross-process ({}) para '{}'", hops, path);
                        exchange.getResponse().setStatusCode(HttpStatus.LOOP_DETECTED);
                        return exchange.getResponse().setComplete();
                    }
                } catch (NumberFormatException ignored) {}
            }

            Boolean isForwarded = exchange.getAttribute("GATEWAY_FORWARDED_REQUEST");
            if (Boolean.TRUE.equals(isForwarded)) {
                if ("/".equals(path)) {
                    String originalPath = exchange.getAttribute("ORIGINAL_PATH");
                    log.error("BUCLE INFINITO DETECTADO: ruta original '{}'", originalPath);
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    String body = String.format(
                        "{\"error\":\"Not Found\",\"message\":\"No se encontró el recurso: %s\",\"path\":\"%s\"}",
                        originalPath != null ? originalPath : path,
                        originalPath != null ? originalPath : path);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                }
                return chain.filter(exchange);
            }

            Boolean processed = exchange.getAttribute("DYNAMIC_MODULE_ROUTE_PROCESSED");
            if (Boolean.TRUE.equals(processed)) {
                return chain.filter(exchange);
            }

            Integer attemptCount = exchange.getAttribute(ROUTING_ATTEMPT_KEY);
            if (attemptCount == null) attemptCount = 0;

            exchange.getAttributes().put("DYNAMIC_MODULE_ROUTE_PROCESSED", true);

            if (attemptCount >= MAX_ROUTING_ATTEMPTS) {
                log.error("LÍMITE DE INTENTOS DE ROUTING EXCEDIDO ({}/{})", attemptCount, MAX_ROUTING_ATTEMPTS);
                exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                return exchange.getResponse().setComplete();
            }

            String requestPath = exchange.getAttribute("ORIGINAL_PATH");
            if (requestPath == null) {
                requestPath = exchange.getRequest().getPath().value();
            }

            final String resolvedPath = requestPath;
            final ServerHttpRequest request = exchange.getRequest();

            Timer.Sample sample = Timer.start();
            return routeResolver.resolve(resolvedPath)
                .flatMap(module -> {
                    // Incrementar contador para CUALQUIER tipo de routing
                    int current = exchange.getAttributeOrDefault(ROUTING_ATTEMPT_KEY, 0);
                    exchange.getAttributes().put(ROUTING_ATTEMPT_KEY, current + 1);
                    return processRouting(module, resolvedPath, request, exchange, chain);
                })
                .doOnSuccess(v -> sample.stop(routingTimer))
                .doOnError(e -> sample.stop(routingTimer))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se encontró configuración para la ruta: {}", resolvedPath);
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    return exchange.getResponse().setComplete();
                }));
        };
    }

    protected Mono<Void> processRouting(ModulesUrl module, String requestPath, ServerHttpRequest request,
                                        ServerWebExchange exchange, GatewayFilterChain chain) {
        String target = module.getUri();

        if (target == null || target.isEmpty() || "monolito-modular".equalsIgnoreCase(target)) {
            log.info("Routing interno (forward): {}", requestPath);
            routingInternal.increment();
            return processRoutingInternal(module, requestPath, request, exchange, chain);
        }

        if (target.startsWith("http://") || target.startsWith("https://")) {
            log.info("Routing externo directo: {}", target);
            routingExternal.increment();
            return proxyRequestBuilder.buildExternalProxy(module, requestPath, target, exchange, chain);
        }

        log.info("Resolviendo servicio en Consul: {}", target);
        routingConsul.increment();
        return serviceRegistryPort.chooseHealthyInstance(target)
            .map(ServiceInstanceRecord::baseUrl)
            .flatMap(serviceUri -> {
                log.info("Servicio resuelto: {} → {}", target, serviceUri);
                return proxyRequestBuilder.buildExternalProxy(module, requestPath, serviceUri, exchange, chain);
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.error("No hay instancias saludables para '{}'", target);
                return serviceUnavailableResponse(exchange, target, requestPath);
            }))
            .onErrorResume(error -> {
                log.error("Error resolviendo '{}' desde Consul: {}", target, error.getMessage(), error);
                return serviceUnavailableResponse(exchange, target, requestPath);
            });
    }

    private Mono<Void> processRoutingInternal(ModulesUrl module, String requestPath, ServerHttpRequest request,
                                              ServerWebExchange exchange, GatewayFilterChain chain) {
        Integer currentCount = exchange.getAttribute(ROUTING_ATTEMPT_KEY);
        final int forwardCount = currentCount == null ? 1 : currentCount;

        String newPath = pathTransformer.stripPrefix(requestPath, module);
        log.info("Forward #{}/{}: '{}' -> '{}'", forwardCount, MAX_ROUTING_ATTEMPTS, requestPath, newPath);

        if (requestPath.equals(newPath)) {
            log.error("BUCLE DETECTADO: nueva ruta igual a la original '{}'", newPath);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        exchange.getAttributes().put("GATEWAY_FORWARDED_REQUEST", true);

        URI originalUri = request.getURI();
        String queryString = originalUri.getRawQuery();
        StringBuilder forwardUriBuilder = new StringBuilder("forward:").append(newPath);
        if (queryString != null && !queryString.isEmpty() && !newPath.contains("?")) {
            forwardUriBuilder.append("?").append(queryString);
        }

        URI forwardUri;
        try {
            forwardUri = new URI(forwardUriBuilder.toString());
        } catch (URISyntaxException e) {
            log.error("Error construyendo URI de forward: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, forwardUri);

        Mono<byte[]> cachedBody = DataBufferUtils.join(exchange.getRequest().getBody())
            .map(dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                DataBufferUtils.release(dataBuffer);
                return content;
            })
            .cache();

        String pathForMutate = newPath.contains("?") ? newPath.substring(0, newPath.indexOf("?")) : newPath;
        ServerHttpRequest modifiedRequest = request.mutate().path(pathForMutate).build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(modifiedRequest).build();
        exchange.getAttributes().forEach((k, v) -> mutatedExchange.getAttributes().put(k, v));

        final String finalNewPath = newPath;
        return cachedBody.defaultIfEmpty(new byte[0])
            .flatMap(bytes -> {
                if (bytes.length > 0) {
                    ServerHttpRequest requestWithBody = new ServerHttpRequestDecorator(modifiedRequest) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            DataBuffer buffer = mutatedExchange.getResponse().bufferFactory().wrap(bytes);
                            return Flux.just(buffer);
                        }
                    };
                    return chain.filter(mutatedExchange.mutate().request(requestWithBody).build());
                }
                return chain.filter(mutatedExchange);
            })
            .doOnSuccess(v -> log.debug("Forward #{} completado: '{}' -> '{}'", forwardCount, requestPath, finalNewPath))
            .doOnError(e -> log.error("Error en forward #{}: '{}' -> '{}': {}", forwardCount, requestPath, finalNewPath, e.getMessage()));
    }

    private Mono<Void> serviceUnavailableResponse(ServerWebExchange exchange, String serviceName, String requestPath) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = String.format(
            "{\"error\":\"Service Unavailable\",\"message\":\"No se pudo resolver el servicio '%s'\",\"path\":\"%s\"}",
            serviceName, requestPath);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
