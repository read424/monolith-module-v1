package com.walrex.role.module_role.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Collections.class)
public interface RolMapper {
    RolMapper INSTANCE = Mappers.getMapper(RolMapper.class);

}
