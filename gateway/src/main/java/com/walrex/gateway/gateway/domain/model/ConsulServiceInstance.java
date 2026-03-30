package com.walrex.gateway.gateway.domain.model;

import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

public record ConsulServiceInstance(
    String serviceId,
    String host,
    int port,
    boolean secure
) implements ServiceInstance {

    public static ConsulServiceInstance from(ServiceInstanceRecord record) {
        return new ConsulServiceInstance(record.serviceName(), record.host(), record.port(), record.secure());
    }

    @Override public String getServiceId() { return serviceId; }
    @Override public String getHost()      { return host; }
    @Override public int getPort()         { return port; }
    @Override public boolean isSecure()    { return secure; }

    @Override
    public URI getUri() {
        return URI.create((secure ? "https" : "http") + "://" + host + ":" + port);
    }

    @Override
    public Map<String, String> getMetadata() { return Map.of(); }
}
