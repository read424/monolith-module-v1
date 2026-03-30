package com.walrex.gateway.gateway.infrastructure.adapters.outbound;

import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import com.walrex.gateway.gateway.application.ports.output.ServiceRegistryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServiceRegistryPort implements ServiceRegistryPort {

    @Override
    public Mono<ServiceInstanceRecord> chooseHealthyInstance(String serviceName) {
        // llamar a Consul con WebClient
        return Mono.empty();
    }

    @Override
    public Flux<ServiceInstanceRecord> findHealthyInstances(String serviceName) {
        // llamar a Consul con WebClient
        return Flux.empty();
    }
}
