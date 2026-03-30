package com.walrex.gateway.gateway.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsulHealthServiceEntry(
    Node Node,
    Service Service,
    List<Check> Checks
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Node(
        String Node,
        String Address,
        String Datacenter
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Service(
        String ID,
        String Service,
        String Address,
        Integer Port
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Check(
        String CheckID,
        String Name,
        String Status
    ) {
    }
}
