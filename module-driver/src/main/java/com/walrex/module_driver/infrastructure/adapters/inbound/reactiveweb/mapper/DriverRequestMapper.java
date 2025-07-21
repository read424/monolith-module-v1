package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request.CreateDriverRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DriverRequestMapper {
    DriverDomain toDomain(CreateDriverRequest request);
}
