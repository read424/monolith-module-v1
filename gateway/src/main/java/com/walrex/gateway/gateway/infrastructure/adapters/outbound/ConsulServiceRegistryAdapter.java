package com.walrex.gateway.gateway.infrastructure.adapters.outbound;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.walrex.gateway.gateway.application.ports.output.ServiceRegistryPort;
import com.walrex.gateway.gateway.domain.model.ConsulHealthServiceEntry;
import com.walrex.gateway.gateway.domain.model.ConsulServiceInstance;
import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.ConsulServiceInstanceListSupplier;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.mapper.ConsulServiceInstanceMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class ConsulServiceRegistryAdapter implements ServiceRegistryPort {

    private final WebClient consulWebClient;
    private final ConsulServiceInstanceMapper consulServiceInstanceMapper;
    private final Counter resolveSuccess;
    private final Counter resolveEmpty;
    private final Counter resolveError;

    @Value("${consul.datacenter:dc1}")
    private String datacenter;

    @Value("${server.port:8088}")
    private int localPort;

    private final ConcurrentHashMap<String, RoundRobinLoadBalancer> loadBalancers = new ConcurrentHashMap<>();

    public ConsulServiceRegistryAdapter(WebClient consulWebClient,
                                        ConsulServiceInstanceMapper consulServiceInstanceMapper,
                                        MeterRegistry meterRegistry) {
        this.consulWebClient = consulWebClient;
        this.consulServiceInstanceMapper = consulServiceInstanceMapper;
        this.resolveSuccess = Counter.builder("gateway.consul.resolve").tag("result", "success").register(meterRegistry);
        this.resolveEmpty   = Counter.builder("gateway.consul.resolve").tag("result", "empty").register(meterRegistry);
        this.resolveError   = Counter.builder("gateway.consul.resolve").tag("result", "error").register(meterRegistry);
    }

    @Override
    public Mono<ServiceInstanceRecord> chooseHealthyInstance(String serviceName) {
        RoundRobinLoadBalancer lb = loadBalancers.computeIfAbsent(serviceName, this::createLoadBalancer);
        return lb.choose()
            .flatMap(response -> {
                if (!response.hasServer()) {
                    log.warn("RoundRobin: no hay instancias disponibles para '{}'", serviceName);
                    resolveEmpty.increment();
                    return Mono.empty();
                }
                ServiceInstance instance = response.getServer();
                log.debug("RoundRobin seleccionó: {}:{} para '{}'", instance.getHost(), instance.getPort(), serviceName);
                resolveSuccess.increment();
                return Mono.just(new ServiceInstanceRecord(
                    instance.getServiceId(),
                    instance.getHost(),
                    instance.getPort(),
                    instance.isSecure()
                ));
            })
            .doOnError(e -> resolveError.increment());
    }

    @Override
    public Flux<ServiceInstanceRecord> findHealthyInstances(String serviceName) {
        return consulWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/health/service/{service}")
                .queryParam("passing", "true")
                .queryParam("dc", datacenter)
                .build(serviceName))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(ConsulHealthServiceEntry.class)
            .map(consulServiceInstanceMapper::toRecord)
            .filter(i -> i.port() > 0)
            .filter(this::isNotSelfInstance)
            .doOnNext(i -> log.debug("Consul instance: {} -> {}:{}", i.serviceName(), i.host(), i.port()))
            .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                .filter(e -> !(e instanceof WebClientResponseException wce
                    && wce.getStatusCode().is4xxClientError()))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
            .onErrorResume(WebClientResponseException.class, e -> {
                if (e.getStatusCode().value() == 403) {
                    log.error("Token ACL rechazado por Consul al consultar '{}': {}", serviceName, e.getStatusCode());
                } else {
                    log.error("Error HTTP {} consultando Consul para '{}': {}", e.getStatusCode().value(), serviceName, e.getMessage());
                }
                return Flux.empty();
            })
            .onErrorResume(e -> {
                log.error("Error de red/timeout consultando Consul para '{}': {}", serviceName, e.getMessage());
                return Flux.empty();
            });
    }

    private boolean isNotSelfInstance(ServiceInstanceRecord instance) {
        if (instance.port() != localPort) {
            return true;
        }
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            boolean isSelf = localIp.equals(instance.host())
                || "127.0.0.1".equals(instance.host())
                || "localhost".equals(instance.host());
            if (isSelf) {
                log.warn("Filtrado self-routing: instancia {}:{} apunta a este gateway",
                    instance.host(), instance.port());
            }
            return !isSelf;
        } catch (UnknownHostException e) {
            log.warn("No se pudo determinar la IP local para filtro self-routing: {}", e.getMessage());
            return true; // en duda, no filtrar
        }
    }

    private RoundRobinLoadBalancer createLoadBalancer(String serviceName) {
        ConsulServiceInstanceListSupplier supplier = new ConsulServiceInstanceListSupplier(
            serviceName,
            name -> findHealthyInstances(name).map(ConsulServiceInstance::from)
        );
        return new RoundRobinLoadBalancer(singletonProvider(supplier), serviceName);
    }

    private static <T> ObjectProvider<T> singletonProvider(T instance) {
        return new ObjectProvider<>() {
            @Override public T getObject()               { return instance; }
            @Override public T getObject(Object... args) { return instance; }
            @Override public T getIfAvailable()          { return instance; }
            @Override public T getIfUnique()             { return instance; }
            @Override public Iterator<T> iterator()      { return Collections.singleton(instance).iterator(); }
        };
    }
}
