package com.walrex.gateway.gateway.application.ports.output;

import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceRegistryPort {
    Mono<ServiceInstanceRecord> chooseHealthyInstance(String serviceName);
    Flux<ServiceInstanceRecord> findHealthyInstances(String serviceName);
}
