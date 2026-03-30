package com.walrex.gateway.gateway.infrastructure.adapters.outbound;

import com.walrex.gateway.gateway.application.ports.output.ServiceRegistryPort;
import com.walrex.gateway.gateway.domain.model.ConsulHealthServiceEntry;
import com.walrex.gateway.gateway.domain.model.ConsulServiceInstance;
import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.ConsulServiceInstanceListSupplier;
import com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.mapper.ConsulServiceInstanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsulServiceRegistryAdapter implements ServiceRegistryPort {

    private final WebClient consulWebClient;
    private final ConsulServiceInstanceMapper consulServiceInstanceMapper;

    @Value("${consul.datacenter:dc1}")
    private String datacenter;

    private final ConcurrentHashMap<String, RoundRobinLoadBalancer> loadBalancers = new ConcurrentHashMap<>();

    @Override
    public Mono<ServiceInstanceRecord> chooseHealthyInstance(String serviceName) {
        RoundRobinLoadBalancer lb = loadBalancers.computeIfAbsent(serviceName, this::createLoadBalancer);
        return lb.choose()
            .flatMap(response -> {
                if (!response.hasServer()) {
                    log.warn("RoundRobin: no hay instancias disponibles para '{}'", serviceName);
                    return Mono.empty();
                }
                ServiceInstance instance = response.getServer();
                log.debug("RoundRobin seleccionó: {}:{} para '{}'", instance.getHost(), instance.getPort(), serviceName);
                return Mono.just(new ServiceInstanceRecord(
                    instance.getServiceId(),
                    instance.getHost(),
                    instance.getPort(),
                    instance.isSecure()
                ));
            });
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
            .doOnNext(i -> log.debug("Consul instance: {} -> {}:{}", i.serviceName(), i.host(), i.port()))
            .timeout(Duration.ofSeconds(3))
            .onErrorResume(e -> {
                log.error("Error consultando Consul para {}: {}", serviceName, e.getMessage());
                return Flux.empty();
            });
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
            @Override public T getObject()                    { return instance; }
            @Override public T getObject(Object... args)      { return instance; }
            @Override public T getIfAvailable()               { return instance; }
            @Override public T getIfUnique()                  { return instance; }
            @Override public Iterator<T> iterator()           { return Collections.singleton(instance).iterator(); }
        };
    }
}
