package com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul;

import com.walrex.gateway.gateway.domain.model.ConsulServiceInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

/**
 * Supplies healthy instances for a given service by delegating to the Consul
 * health API via the provided instanceSource function.
 *
 * The function indirection breaks the circular dependency that would arise if
 * this class held a direct reference to ConsulServiceRegistryAdapter.
 */
@RequiredArgsConstructor
public class ConsulServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final String serviceId;
    private final Function<String, Flux<ConsulServiceInstance>> instanceSource;

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return instanceSource.apply(serviceId)
            .cast(ServiceInstance.class)
            .collectList()
            .flux();
    }
}
