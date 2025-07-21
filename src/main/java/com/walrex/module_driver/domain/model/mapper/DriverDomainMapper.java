package com.walrex.module_driver.domain.model.mapper;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DriverDomainMapper {
    CreateDriverDTO toDTO(DriverDomain createDriverDTO);
}
