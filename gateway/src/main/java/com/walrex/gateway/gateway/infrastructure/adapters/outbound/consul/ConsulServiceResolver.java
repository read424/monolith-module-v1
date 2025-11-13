package com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resuelve servicios desde Consul Service Discovery
 *
 * Este componente permite resolver dinámicamente la ubicación (IP:PORT) de servicios
 * registrados en Consul, habilitando el routing transparente desde el Gateway hacia
 * microservicios externos.
 *
 * Ejemplo:
 * - serviceName: "quarkus-message-service"
 * - Resuelve a: http://192.168.1.60:8088
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsulServiceResolver {

    private final DiscoveryClient discoveryClient;

    /**
     * Resuelve la URI completa de un servicio registrado en Consul
     *
     * @param serviceName Nombre del servicio en Consul (ej: "quarkus-message-service")
     * @return Mono con la URI completa (ej: "http://192.168.1.60:8088")
     */
    public Mono<String> resolveServiceUri(String serviceName) {
        return resolveService(serviceName)
            .map(instance -> {
                String scheme = instance.isSecure() ? "https" : "http";
                String uri = String.format("%s://%s:%d",
                    scheme,
                    instance.getHost(),
                    instance.getPort()
                );
                log.info("🌐 URI construida: {} para servicio: {}", uri, serviceName);
                return uri;
            });
    }

    /**
     * Resuelve una instancia del servicio desde Consul
     *
     * Implementa load balancing simple mediante selección aleatoria cuando
     * existen múltiples instancias del mismo servicio.
     *
     * @param serviceName Nombre del servicio registrado en Consul
     * @return Mono con la instancia seleccionada
     */
    public Mono<ServiceInstance> resolveService(String serviceName) {
        return Mono.fromCallable(() -> {
            log.debug("🔍 Consultando Consul para servicio: {}", serviceName);

            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

            if (instances == null || instances.isEmpty()) {
                log.error("❌ No se encontraron instancias para servicio: {}", serviceName);
                return null;
            }

            // Load balancing simple: Selección aleatoria
            ServiceInstance selected = instances.get(
                ThreadLocalRandom.current().nextInt(instances.size())
            );

            log.info("✅ Servicio resuelto: {} → {}:{} (instancias disponibles: {})",
                serviceName,
                selected.getHost(),
                selected.getPort(),
                instances.size());

            return selected;
        })
        .subscribeOn(Schedulers.boundedElastic()); // Non-blocking I/O
    }

    /**
     * Verifica si un servicio está registrado en Consul
     *
     * @param serviceName Nombre del servicio
     * @return Mono<Boolean> true si el servicio existe
     */
    public Mono<Boolean> isServiceAvailable(String serviceName) {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            boolean available = instances != null && !instances.isEmpty();
            log.debug("🔍 Servicio {} disponible: {}", serviceName, available);
            return available;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}