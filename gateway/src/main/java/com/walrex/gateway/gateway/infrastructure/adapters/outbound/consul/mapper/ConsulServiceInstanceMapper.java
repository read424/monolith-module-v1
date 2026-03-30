package com.walrex.gateway.gateway.infrastructure.adapters.outbound.consul.mapper;

import com.walrex.gateway.gateway.domain.model.ConsulHealthServiceEntry;
import com.walrex.gateway.gateway.domain.model.ServiceInstanceRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConsulServiceInstanceMapper {

    @Mapping(target = "serviceName", expression = "java(resolveServiceName(entry))")
    @Mapping(target = "host", expression = "java(resolveHost(entry))")
    @Mapping(target = "port", expression = "java(resolvePort(entry))")
    @Mapping(target = "secure", constant = "false")
    ServiceInstanceRecord toRecord(ConsulHealthServiceEntry entry);

    default String resolveServiceName(ConsulHealthServiceEntry entry) {
        if (entry == null || entry.Service() == null || entry.Service().Service() == null) {
            return "unknown";
        }
        return entry.Service().Service();
    }

    default String resolveHost(ConsulHealthServiceEntry entry) {
        if (entry == null) {
            return null;
        }

        String serviceAddress = entry.Service() != null ? entry.Service().Address() : null;
        String nodeAddress = entry.Node() != null ? entry.Node().Address() : null;

        if (serviceAddress != null && !serviceAddress.isBlank()) {
            return serviceAddress;
        }

        return nodeAddress;
    }

    default int resolvePort(ConsulHealthServiceEntry entry) {
        if (entry == null || entry.Service() == null || entry.Service().Port() == null) {
            return 0;
        }
        return entry.Service().Port();
    }
}
