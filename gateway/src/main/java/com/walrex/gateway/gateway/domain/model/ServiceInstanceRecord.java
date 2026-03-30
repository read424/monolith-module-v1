package com.walrex.gateway.gateway.domain.model;

public record ServiceInstanceRecord(
    String serviceName,
    String host,
    int port,
    boolean secure
) {
    public String baseUrl() {
        String scheme = secure ? "https" : "http";
        return scheme + "://" + host + ":" + port;
    }
}
